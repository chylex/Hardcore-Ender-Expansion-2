package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.entity.cloneFrom
import chylex.hee.game.inventory.cleanupNBT
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.item.ItemTableLink.Companion.SoundType.LINK_FAIL
import chylex.hee.game.item.ItemTableLink.Companion.SoundType.LINK_OUTPUT
import chylex.hee.game.item.ItemTableLink.Companion.SoundType.LINK_RESTART
import chylex.hee.game.item.ItemTableLink.Companion.SoundType.LINK_SUCCESS
import chylex.hee.game.item.components.UseOnBlockComponent
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getTile
import chylex.hee.game.world.playClient
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.IFxData
import chylex.hee.network.fx.IFxHandler
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.getPos
import chylex.hee.system.serialization.hasKey
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.readPos
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writePos
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ActionResultType
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class ItemTableLink(properties: Properties) : ItemWithComponents(properties), UseOnBlockComponent {
	companion object {
		private const val POS_TAG = "StoredPos"
		private const val TIME_TAG = "StoredTime"
		
		private const val RESET_TIME_TICKS = 20 * 8
		
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
				
				when(SoundType.values().getOrNull(readByte().toInt())) {
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
	
	init {
		components.attach(this)
	}
	
	override fun useOnBlock(world: World, pos: BlockPos, player: EntityPlayer, item: ItemStack, ctx: ItemUseContext): ActionResultType? {
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
		
		player.cooldownTracker.setCooldown(this, 5)
		
		if (world.isRemote) {
			return SUCCESS
		}
		
		val heldItem = player.getHeldItem(ctx.hand)
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
				putLong(TIME_TAG, world.totalTime)
			}
			
			PacketClientFX(FX_USE, FxUseData(pos, soundType)).sendToAllAround(world, pos, 16.0)
		}
		
		return SUCCESS
	}
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		if (world.isRemote || world.totalTime % 10L != 0L) {
			return
		}
		
		val tag = stack.heeTagOrNull
		
		if (tag.hasKey(TIME_TAG) && world.totalTime - tag.getLong(TIME_TAG) >= RESET_TIME_TICKS) {
			removeLinkingTags(stack)
		}
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean {
		return stack.heeTagOrNull.hasKey(POS_TAG) // must check, otherwise createEntity spins into infinite recursion
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity {
		val cleanStack = stack.copy().apply(::removeLinkingTags)
		return EntityItem(world, replacee.posX, replacee.posY, replacee.posZ, cleanStack).apply { cloneFrom(replacee) }
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean {
		return stack.heeTagOrNull.hasKey(POS_TAG)
	}
}
