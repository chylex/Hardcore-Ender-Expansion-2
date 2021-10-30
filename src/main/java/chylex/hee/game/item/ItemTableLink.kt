package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.entity.util.cloneFrom
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.item.ItemTableLink.SoundType.LINK_FAIL
import chylex.hee.game.item.ItemTableLink.SoundType.LINK_OUTPUT
import chylex.hee.game.item.ItemTableLink.SoundType.LINK_RESTART
import chylex.hee.game.item.ItemTableLink.SoundType.LINK_SUCCESS
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemEntityComponent
import chylex.hee.game.item.components.IItemGlintComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.util.cleanupNBT
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writePos
import chylex.hee.util.nbt.getPos
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putPos
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

object ItemTableLink : HeeItemBuilder() {
	private const val POS_TAG = "StoredPos"
	private const val TIME_TAG = "StoredTime"
	
	private const val RESET_TIME_TICKS = 20 * 8
	
	init {
		components.glint = IItemGlintComponent { it.heeTagOrNull.hasKey(POS_TAG) }
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override val requiresEditPermission
				get() = false
			
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (!player.isSneaking || !isValidTarget(world, pos)) {
					val pedestal = pos.getTile<TileEntityTablePedestal>(world)
					
					if (pedestal != null) {
						if (!world.isRemote && pedestal.requestMarkAsOutput()) {
							PacketClientFX(FX_USE, FxUseData(pos, LINK_OUTPUT)).sendToAllAround(world, pos, 16.0)
						}
						
						return SUCCESS
					}
					
					return FAIL
				}
				
				player.cooldownTracker.setCooldown(heldItem.item, 5)
				
				if (world.isRemote) {
					return SUCCESS
				}
				
				var newStoredPos = pos
				var soundType = LINK_RESTART
				
				with(heldItem.heeTag) {
					if (hasKey(POS_TAG)) {
						val oldStoredPos = getPos(POS_TAG)
						val tiles = getTablePedestalPair(world, pos, oldStoredPos)
						
						if (tiles != null) {
							val (pedestal, table) = tiles
							
							if (table.tryLinkPedestal(pedestal)) {
								heldItem.shrink(1)
								newStoredPos = table.pos
								soundType = if (pedestal.isDedicatedOutput) LINK_OUTPUT else LINK_SUCCESS
							}
							else {
								newStoredPos = oldStoredPos
								soundType = LINK_FAIL
							}
						}
					}
					
					if (heldItem.isNotEmpty) {
						putPos(POS_TAG, newStoredPos)
						putLong(TIME_TAG, world.gameTime)
					}
					
					PacketClientFX(FX_USE, FxUseData(pos, soundType)).sendToAllAround(world, pos, 16.0)
				}
				
				return SUCCESS
			}
		}
		
		components.tickInInventory.add(object : ITickInInventoryComponent {
			override fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean) {
				if (world.isRemote || world.gameTime % 10L != 0L) {
					return
				}
				
				val tag = stack.heeTagOrNull
				if (tag.hasKey(TIME_TAG) && world.gameTime - tag.getLong(TIME_TAG) >= RESET_TIME_TICKS) {
					removeLinkingTags(stack)
				}
			}
		})
		
		components.itemEntity = object : IItemEntityComponent {
			override fun hasEntity(stack: ItemStack): Boolean {
				return stack.heeTagOrNull.hasKey(POS_TAG) // must check, otherwise createEntity spins into infinite recursion
			}
			
			override fun createEntity(world: World, stack: ItemStack, replacee: Entity): ItemEntity {
				val cleanStack = stack.copy().apply(::removeLinkingTags)
				return ItemEntity(world, replacee.posX, replacee.posY, replacee.posZ, cleanStack).apply { cloneFrom(replacee) }
			}
		}
	}
	
	enum class SoundType {
		LINK_FAIL, LINK_OUTPUT, LINK_RESTART, LINK_SUCCESS
	}
	
	class FxUseData(private val pos: BlockPos, private val type: SoundType) : IFxData {
		override fun write(buffer: PacketBuffer) = buffer.use {
			writePos(pos)
			writeByte(type.ordinal)
		}
	}
	
	val FX_USE = object : IFxHandler<FxUseData> {
		override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
			val pos = readPos()
			
			when (SoundType.values().getOrNull(readByte().toInt())) {
				LINK_SUCCESS -> ModSounds.ITEM_TABLE_LINK_USE_SUCCESS.playClient(pos, SoundCategory.PLAYERS, pitch = rand.nextFloat(0.49F, 0.51F))
				LINK_RESTART -> ModSounds.ITEM_TABLE_LINK_USE_SPECIAL.playClient(pos, SoundCategory.PLAYERS, pitch = rand.nextFloat(0.69F, 0.71F))
				LINK_OUTPUT  -> ModSounds.ITEM_TABLE_LINK_USE_SPECIAL.playClient(pos, SoundCategory.PLAYERS, volume = 0.9F, pitch = 0.63F)
				else         -> ModSounds.ITEM_TABLE_LINK_USE_FAIL.playClient(pos, SoundCategory.PLAYERS, volume = 0.9F, pitch = rand.nextFloat(0.72F, 0.73F))
			}
		}
	}
	
	private fun isValidTarget(world: World, pos: BlockPos): Boolean {
		val block = pos.getBlock(world)
		return block is BlockAbstractTableTile<*> || (block === ModBlocks.TABLE_PEDESTAL && pos.getTile<TileEntityTablePedestal>(world)?.hasLinkedTable == false)
	}
	
	private fun getTablePedestalPair(world: World, pos1: BlockPos, pos2: BlockPos): Pair<TileEntityTablePedestal, TileEntityBaseTable>? {
		val pedestal: TileEntityTablePedestal
		val table: TileEntityBaseTable
		
		if (pos1.getBlock(world) === ModBlocks.TABLE_PEDESTAL) {
			pedestal = pos1.getTile(world) ?: return null
			table    = pos2.getTile(world) ?: return null
		}
		else {
			pedestal = pos2.getTile(world) ?: return null
			table    = pos1.getTile(world) ?: return null
		}
		
		return Pair(pedestal, table)
	}
	
	private fun removeLinkingTags(stack: ItemStack) {
		stack.heeTagOrNull?.apply {
			remove(POS_TAG)
			remove(TIME_TAG)
		}
		
		stack.cleanupNBT()
	}
}
