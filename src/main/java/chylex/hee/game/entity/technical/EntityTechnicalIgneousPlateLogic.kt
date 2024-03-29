package chylex.hee.game.entity.technical

import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.entity.IHeeEntityType
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.entity.util.selectEntities
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.setBlock
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.heeTag
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.writePos
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItemOrNull
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.particles.ParticleTypes.CLOUD
import net.minecraft.particles.ParticleTypes.LARGE_SMOKE
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.tileentity.FurnaceTileEntity
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class EntityTechnicalIgneousPlateLogic(type: EntityType<EntityTechnicalIgneousPlateLogic>, world: World) : Entity(type, world) {
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.IGNEOUS_PLATE_LOGIC, world)
	
	object Type : IHeeEntityType<EntityTechnicalIgneousPlateLogic> {
		override val size
			get() = EntitySize(0F)
		
		override val tracker
			get() = EntityTrackerInfo(trackingRange = 2, updateInterval = 10, receiveVelocityUpdates = false)
		
		override val isImmuneToFire
			get() = true
	}
	
	companion object {
		private const val FIELD_COOK_TIME_CURRENT = 2
		private const val FIELD_COOK_TIME_TARGET = 3
		
		private const val OVERHEAT_LEVEL_LIMIT = 1899F
		
		private val DATA_OVERHEAT_LEVEL = EntityData.register<EntityTechnicalIgneousPlateLogic, Float>(DataSerializers.FLOAT)
		
		private const val EXTRA_TICKS_TAG = "ExtraTicks"
		private const val OVERHEAT_LEVEL_TAG = "OverheatLevel"
		
		private fun getAttachedPlates(furnace: FurnaceTileEntity): List<TileEntityIgneousPlate> {
			val world = furnace.world ?: return emptyList()
			val pos = furnace.pos
			
			return BlockIgneousPlate.FACING_NOT_DOWN.allowedValues.mapNotNull { pos.offset(it).getTile<TileEntityIgneousPlate>(world)?.takeIf { plate -> plate.isAttachedTo(furnace) } }
		}
		
		private fun findLogicEntity(furnace: FurnaceTileEntity): EntityTechnicalIgneousPlateLogic? {
			return furnace.world?.selectEntities?.inBox<EntityTechnicalIgneousPlateLogic>(AxisAlignedBB(furnace.pos))?.firstOrNull()
		}
		
		fun createForFurnace(furnace: FurnaceTileEntity) {
			if (findLogicEntity(furnace) == null) {
				furnace.world!!.spawn(ModEntities.IGNEOUS_PLATE_LOGIC, furnace.pos.center)
			}
		}
		
		fun getOverheatingPercentage(furnace: FurnaceTileEntity): Float {
			val entity = findLogicEntity(furnace) ?: return 0F
			return entity.overheatLevel / OVERHEAT_LEVEL_LIMIT
		}
		
		fun triggerCooling(furnace: FurnaceTileEntity): Boolean {
			val entity = findLogicEntity(furnace) ?: return false
			val prevOverheat = entity.overheatLevel
			
			if (prevOverheat == 0F) {
				return false
			}
			
			val newOverheat = prevOverheat * 0.5F
			val reductionRatio = (prevOverheat - newOverheat) / OVERHEAT_LEVEL_LIMIT
			val plateSpeedReduction = reductionRatio * 0.5F
			
			entity.overheatLevel = newOverheat
			
			for (plate in getAttachedPlates(furnace)) {
				plate.reduceSpeed(plateSpeedReduction)
			}
			
			PacketClientFX(FX_COOLING, FxCoolingData(furnace.pos, reductionRatio)).sendToAllAround(furnace, 24.0)
			return true
		}
		
		private fun dropOverheatItem(world: World, pos: BlockPos, stack: ItemStack) {
			val rand = world.rand
			val target = pos.center.add(rand.nextFloat(-0.24, 0.24), 0.0, rand.nextFloat(-0.24, 0.24))
			
			while (stack.isNotEmpty) {
				EntityItemFreshlyCooked(world, target, stack.split(rand.nextInt(10, 20))).apply {
					motion = Vec3.ZERO
					world.addEntity(this)
				}
			}
		}
		
		private val PARTICLE_COOLING = ParticleSpawnerVanilla(
			type = SMOKE,
			pos = InBox(0.45F)
		)
		
		private val PARTICLE_OVERHEAT = ParticleSpawnerVanilla(
			type = LARGE_SMOKE,
			pos = Constant(0.25F, UP) + InBox(1.75F, 0.5F, 1.75F)
		)
		
		private val PARTICLE_OVERHEAT_CLOUD = ParticleSpawnerVanilla(
			type = CLOUD,
			pos = Constant(0.25F, UP) + InBox(1.75F, 0.5F, 1.75F)
		)
		
		class FxCoolingData(private val pos: BlockPos, private val amount: Float) : IFxData {
			override fun write(buffer: PacketBuffer) {
				buffer.writePos(pos)
				buffer.writeFloat(amount)
			}
		}
		
		val FX_COOLING = object : IFxHandler<FxCoolingData> {
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
				val pos = buffer.readPos()
				val amount = buffer.readFloat()
				
				pos.getTile<FurnaceTileEntity>(world)?.let(::getAttachedPlates)?.forEach {
					PARTICLE_COOLING.spawn(Point(it.pos, 5 + (10 * amount).floorToInt()), rand)
				}
				
				ModSounds.BLOCK_IGNEOUS_PLATE_COOL.playClient(pos, SoundCategory.BLOCKS, volume = 0.25F + (1.75F * amount), pitch = 0.8F)
			}
		}
		
		val FX_OVERHEAT = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				PARTICLE_OVERHEAT.spawn(Point(pos, 50), rand)
				PARTICLE_OVERHEAT_CLOUD.spawn(Point(pos, 15), rand)
				SoundEvents.ENTITY_GENERIC_EXPLODE.playClient(pos, SoundCategory.BLOCKS, volume = 2F, pitch = 1F)
			}
		}
	}
	
	// Instance
	
	private var extraTicks = 0.0
	private var overheatLevel by EntityData(DATA_OVERHEAT_LEVEL)
	
	init {
		noClip = true
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun registerData() {
		dataManager.register(DATA_OVERHEAT_LEVEL, 0F)
	}
	
	override fun tick() {
		super.tick()
		
		if (!world.isRemote && world.isAreaLoaded(position, 1)) {
			val furnace = Pos(this).getTile<FurnaceTileEntity>(world)
			
			if (furnace == null) {
				remove()
				return
			}
			
			val plates = getAttachedPlates(furnace)
			
			if (plates.isEmpty()) {
				remove()
				return
			}
			
			val speedMultiplier = (plates.sumOf(TileEntityIgneousPlate::potential) / plates.size) * 2.0 * plates.size.toDouble().pow(0.793)
			increaseFurnaceTicks(furnace, speedMultiplier - 1.0)
			
			if (speedMultiplier < 2.1) {
				if (overheatLevel > 0F) {
					overheatLevel = max(0F, (overheatLevel - 0.25F) * (if (furnace.isBurning) 0.9996F else 0.998F))
				}
			}
			else if (furnace.isBurning) {
				increaseHeatLevel(furnace, (speedMultiplier - 2.2).toFloat())
			}
			else if (plates.any { it.potential > 0.05 }) {
				increaseHeatLevel(furnace, ((speedMultiplier - 2.2) * 0.25).toFloat())
			}
		}
	}
	
	// Furnace handling
	
	private fun increaseFurnaceTicks(furnace: FurnaceTileEntity, amount: Double) {
		extraTicks += max(0.0, amount)
		
		while (extraTicks >= 1.0) {
			extraTicks -= 1.0
			
			if (furnace.isBurning) {
				val data = furnace.furnaceData
				
				val currentCookTime = data.get(FIELD_COOK_TIME_CURRENT)
				val newCookTime = min(currentCookTime + 1, data.get(FIELD_COOK_TIME_TARGET) - 1)
				
				data.set(FIELD_COOK_TIME_CURRENT, newCookTime)
			}
		}
	}
	
	private fun increaseHeatLevel(furnace: FurnaceTileEntity, amount: Float) {
		overheatLevel += max(0F, amount)
		
		if (overheatLevel > OVERHEAT_LEVEL_LIMIT) {
			for (plate in getAttachedPlates(furnace)) {
				plate.blastOff()
			}
			
			val pos = Pos(this)
			val freePositions = pos.allInCenteredBox(1, 0, 1).toList().filter { it.isAir(world) }
			
			val ingredientSlots = furnace.getSlotsForFace(UP)
			
			for ((slot, stack) in furnace.nonEmptySlots) {
				if (slot in ingredientSlots) {
					stack.shrink(1)
				}
				
				dropOverheatItem(world, rand.nextItemOrNull(freePositions) ?: pos, stack)
			}
			
			furnace.clear()
			
			if (furnace.javaClass === FurnaceTileEntity::class.java) {
				repeat(5) {
					dropOverheatItem(world, rand.nextItemOrNull(freePositions) ?: pos, ItemStack(Blocks.COBBLESTONE))
				}
			}
			
			PacketClientFX(FX_OVERHEAT, FxBlockData(pos)).sendToAllAround(this, 32.0)
			
			pos.setBlock(world, Blocks.FIRE)
			remove()
		}
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putDouble(EXTRA_TICKS_TAG, extraTicks)
		putFloat(OVERHEAT_LEVEL_TAG, overheatLevel)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		extraTicks = getDouble(EXTRA_TICKS_TAG)
		overheatLevel = getFloat(OVERHEAT_LEVEL_TAG)
	}
}
