package chylex.hee.game.block.entity
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.ModSounds
import chylex.hee.init.ModTileEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.migration.Facing
import chylex.hee.system.migration.vanilla.BlockRotatedPillar
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.NBTItemStackList
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.center
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.getHardness
import chylex.hee.system.util.getListOfItemStacks
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.playClient
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.with
import chylex.hee.system.util.writeCompactVec
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import kotlin.math.max
import kotlin.math.roundToInt

class TileEntityMinersBurialAltar(type: TileEntityType<TileEntityMinersBurialAltar>) : TileEntityBase(type), ITickableTileEntity{
	constructor() : this(ModTileEntities.MINERS_BURIAL_ALTAR)
	
	companion object{
		private const val HAS_MEDALLION_TAG = "HasMedallion"
		private const val REDEEM_TYPE_TAG = "RedeemType"
		private const val REDEEM_TICK_TAG = "RedeemTick"
		private const val ITEM_DROPS_TAG = "ItemDrops"
		
		private const val REDEEM_TYPE_TOKEN: Byte = 0
		private const val REDEEM_TYPE_ITEMS: Byte = 1
		private const val REDEEM_TYPE_BOSS: Byte = 2
		private const val REDEEM_TYPE_FINISHED: Byte = 3
		
		private const val MEDALLION_ANIM_DELAY = 11
		private const val MEDALLION_ANIM_MAIN = 37
		private const val MEDALLION_ANIM_TOTAL_DURATION = (2 * MEDALLION_ANIM_DELAY) + MEDALLION_ANIM_MAIN
		
		private const val PILLAR_HEIGHT = 4
		
		private val ITEM_DROP_TABLES_MAIN = arrayOf(
			weightedListOf(
				220 to (Items.IRON_INGOT      to 13..17),
				175 to (Items.GOLD_INGOT      to  9..12),
				 30 to (ModItems.ENDIUM_INGOT to  4..5)
			),
			weightedListOf(
				80 to (Items.IRON_NUGGET      to 17..21),
				70 to (Items.GOLD_NUGGET      to 15..20),
				50 to (ModItems.ENDIUM_NUGGET to 10..13)
			),
			weightedListOf(
				45 to (Items.EMERALD to 7..10),
				55 to (Items.DIAMOND to 4..6)
			)
			// TODO modded ingots, nuggets, and gems
		)
		
		private val ITEM_DROP_TABLE_MISC = weightedListOf(
			140 to (Items.COAL        to 15..20),
			115 to (Items.REDSTONE    to 12..15),
			100 to (Items.QUARTZ      to 11..14),
			 75 to (Items.ENDER_PEARL to  5..8)
		)
		
		private val PARTICLE_SPAWN = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = RGB(255u), scale = 0.8F),
			pos = InBox(0.175F)
		)
		
		class FxSpawnData(private val pos: Vec3d, private val type: Byte) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writeCompactVec(pos)
				writeByte(type.toInt())
			}
		}
		
		val FX_SPAWN = object : IFxHandler<FxSpawnData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val pos = readCompactVec()
				
				when(readByte()){
					REDEEM_TYPE_TOKEN -> {
						PARTICLE_SPAWN.spawn(Point(pos, 9), rand)
					}
					
					REDEEM_TYPE_ITEMS -> {
						PARTICLE_SPAWN.spawn(Point(pos, 4), rand)
						ModSounds.BLOCK_MINERS_BURIAL_ALTAR_SPAWN.playClient(pos, SoundCategory.BLOCKS, volume = 0.2F, pitch = if (rand.nextInt(4) == 0) 0.95F else 1F)
					}
					
					REDEEM_TYPE_FINISHED -> {
						for(y in 0 until PILLAR_HEIGHT){
							MC.particleManager.addBlockDestroyEffects(Pos(pos).up(y), ModBlocks.MINERS_BURIAL_BLOCK_PILLAR.defaultState)
						}
						
						ModSounds.BLOCK_MINERS_BURIAL_ALTAR_DONE.playClient(pos, SoundCategory.BLOCKS, volume = 1.2F, pitch = 0.7F)
					}
				}
			}
		}
	}
	
	var hasMedallion by Notifying(false, FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	
	val clientMedallionAnimProgress
		get() = (redeemTick - MEDALLION_ANIM_DELAY).coerceIn(0, MEDALLION_ANIM_MAIN) / MEDALLION_ANIM_MAIN.toFloat()
	
	private var redeemType = REDEEM_TYPE_TOKEN
	private var redeemTick = 0
	
	private val itemDrops = mutableListOf<ItemStack>()
	
	// Ticking
	
	override fun tick(){
		val closestPlayer = wrld.getClosestPlayer(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 12.0, false)
		
		if (closestPlayer == null){
			return
		}
		
		if (!hasMedallion){
			redeemTick = 0
			return
		}
		
		if (wrld.isRemote){
			if (redeemTick > 0 && redeemType != REDEEM_TYPE_FINISHED){
				++redeemTick
				// TODO sound of huge stones rubbing against each other for the insertion animation
			}
			
			return
		}
		
		if (redeemType == REDEEM_TYPE_FINISHED){
			if (--redeemTick < 0){
				val pillar = ModBlocks.MINERS_BURIAL_BLOCK_PILLAR.with(BlockRotatedPillar.AXIS, Facing.AXIS_Y)
				
				for(y in 1 until PILLAR_HEIGHT){
					val offsetPos = pos.up(y)
					
					if (offsetPos.getHardness(wrld) >= 0F){
						offsetPos.setState(wrld, pillar)
					}
				}
				
				pos.setState(wrld, pillar)
				
				PacketClientFX(FX_SPAWN, FxSpawnData(pos.center, redeemType)).sendToAllAround(this, 16.0)
				return
			}
			
			markDirty()
		}
		else if (redeemTick > 0 || (redeemTick == 0 && wrld.tickableTileEntities.none { it is TileEntityMinersBurialAltar && it.redeemTick > 0 && isGrouped(it) })){
			++redeemTick
			
			if (redeemTick == 1){
				notifyUpdate(FLAG_SYNC_CLIENT)
			}
			else if (redeemTick > MEDALLION_ANIM_TOTAL_DURATION && (tickRedeemSequence(redeemTick - 1 - MEDALLION_ANIM_TOTAL_DURATION, closestPlayer) || redeemTick == Int.MAX_VALUE)){
				for(tile in wrld.tickableTileEntities){
					if (tile is TileEntityMinersBurialAltar && tile !== this && isGrouped(tile)){
						tile.redeemType = ((redeemType + 1) % 3).toByte()
						tile.markDirty()
					}
				}
				
				redeemType = REDEEM_TYPE_FINISHED
			}
			
			markDirty()
		}
	}
	
	private fun tickRedeemSequence(tick: Int, closestPlayer: EntityPlayer): Boolean{
		if (redeemType == REDEEM_TYPE_TOKEN){
			if (tick in 1..38){
				return false
			}
			
			val tokenHolderPos = pos.up(1)
			val tokenHolder = wrld.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(tokenHolderPos)).firstOrNull()
			
			if (tick == 0){
				if (tokenHolder == null){
					val fxPos = Vec3d(tokenHolderPos.x + 0.5, tokenHolderPos.y + 0.95, tokenHolderPos.z + 0.5)
					PacketClientFX(FX_SPAWN, FxSpawnData(fxPos, redeemType)).sendToAllAround(wrld, fxPos, 16.0)
					
					EntityTokenHolder(wrld, tokenHolderPos, TokenType.NORMAL, TerritoryType.CURSED_LIBRARY).apply {
						currentCharge = 0F
						wrld.addEntity(this)
					}
				}
				
				return false
			}
			
			if (tokenHolder != null){
				val newCharge = tokenHolder.currentCharge + (1F / 90F)
				
				if (newCharge < 1F){
					tokenHolder.currentCharge = newCharge
					// TODO fx
				}
				else{
					tokenHolder.forceDropTokenTowards(closestPlayer)
					tokenHolder.remove()
					
					redeemTick = 20
					return true
				}
			}
			
			return false
		}
		else if (redeemType == REDEEM_TYPE_ITEMS){
			if (tick == 0){
				itemDrops.clear()
				
				val rand = wrld.rand
				val mainTables = ITEM_DROP_TABLES_MAIN.map { it.mutableCopy() }
				
				for(table in mainTables){
					table.removeItem(rand)?.let(::addItemDrop)
				}
				
				val combinedTable = mutableWeightedListOf<Pair<Item, IntRange>>().apply {
					mainTables.forEach(::addItems)
					addItems(ITEM_DROP_TABLE_MISC)
				}
				
				repeat(6 - itemDrops.size){
					combinedTable.removeItem(rand)?.let(::addItemDrop)
				}
				
				rand.nextItem(itemDrops).let { it.size = max(1, (it.size * 1.4).roundToInt()) }
				rand.nextItem(itemDrops).let { it.size = max(1, (it.size * 0.7).roundToInt()) }
				
				itemDrops.shuffle(rand)
				return false
			}
			
			if (tick % 4 == 0){
				if (itemDrops.isEmpty()){
					redeemTick = 14
					return true
				}
				
				val rand = wrld.rand
				
				val stack = itemDrops.last()
				val split = stack.split(rand.nextInt(1, 3))
				
				if (stack.isEmpty){
					itemDrops.removeAt(itemDrops.lastIndex)
				}
				
				val spawnPos = pos.center.addY(0.2)
				val spawnMot = spawnPos.directionTowards(closestPlayer.lookPosVec).scale(0.175).addY(0.3)
				
				val fxPos = spawnPos.addY(0.375)
				PacketClientFX(FX_SPAWN, FxSpawnData(fxPos, redeemType)).sendToAllAround(wrld, fxPos, 16.0)
				
				EntityItem(wrld, spawnPos.x, spawnPos.y, spawnPos.z, split).apply {
					motionVec = spawnMot
					setNoPickupDelay()
					wrld.addEntity(this)
				}
			}
			
			return false
		}
		else if (redeemType == REDEEM_TYPE_BOSS){
			// TODO
		}
		
		return true
	}
	
	// Utilities
	
	private fun isGrouped(other: TileEntityMinersBurialAltar): Boolean{
		return other.pos.distanceSq(pos) < square(8)
	}
	
	private fun addItemDrop(item: Pair<Item, IntRange>){
		itemDrops.add(ItemStack(item.first, wrld.rand.nextInt(item.second)))
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		putBoolean(HAS_MEDALLION_TAG, hasMedallion)
		putByte(REDEEM_TYPE_TAG, redeemType)
		putInt(REDEEM_TICK_TAG, redeemTick)
		
		if (context == STORAGE){
			putList(ITEM_DROPS_TAG, NBTItemStackList.of(itemDrops))
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		hasMedallion = getBoolean(HAS_MEDALLION_TAG)
		redeemType = getByte(REDEEM_TYPE_TAG)
		redeemTick = getInt(REDEEM_TICK_TAG)
		
		if (context == STORAGE){
			itemDrops.clear()
			itemDrops.addAll(getListOfItemStacks(ITEM_DROPS_TAG))
		}
	}
}
