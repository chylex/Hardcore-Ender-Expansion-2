package chylex.hee.game.entity.technical
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.center
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.playClient
import chylex.hee.system.util.readPos
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.EnumParticleTypes.CLOUD
import net.minecraft.util.EnumParticleTypes.SMOKE_LARGE
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class EntityTechnicalIgneousPlateLogic(world: World) : Entity(world){
	companion object{
		private const val FIELD_COOK_TIME_CURRENT = 2
		private const val FIELD_COOK_TIME_TARGET = 3
		
		private const val OVERHEAT_LEVEL_LIMIT = 1899F
		
		private val DATA_OVERHEAT_LEVEL = EntityData.register<EntityTechnicalIgneousPlateLogic, Float>(DataSerializers.FLOAT)
		
		private const val EXTRA_TICKS_TAG = "ExtraTicks"
		private const val OVERHEAT_LEVEL_TAG = "OverheatLevel"
		
		private fun getAttachedPlates(furnace: TileEntityFurnace): List<TileEntityIgneousPlate>{
			val world = furnace.world
			val pos = furnace.pos
			
			return BlockIgneousPlate.FACING_NOT_DOWN.allowedValues.mapNotNull { pos.offset(it).getTile<TileEntityIgneousPlate>(world)?.takeIf { plate -> plate.isAttachedTo(furnace) } }
		}
		
		private fun findLogicEntity(furnace: TileEntityFurnace): EntityTechnicalIgneousPlateLogic?{
			return furnace.world.selectEntities.inBox<EntityTechnicalIgneousPlateLogic>(AxisAlignedBB(furnace.pos)).firstOrNull()
		}
		
		fun createForFurnace(furnace: TileEntityFurnace){
			if (findLogicEntity(furnace) == null){
				val (x, y, z) = furnace.pos.center
				
				EntityTechnicalIgneousPlateLogic(furnace.world).apply {
					setLocationAndAngles(x, y, z, 0F, 0F)
					world.spawnEntity(this)
				}
			}
		}
		
		fun getOverheatingPercentage(furnace: TileEntityFurnace): Float{
			val entity = findLogicEntity(furnace) ?: return 0F
			return entity.overheatLevel / OVERHEAT_LEVEL_LIMIT
		}
		
		fun triggerCooling(furnace: TileEntityFurnace): Boolean{
			val entity = findLogicEntity(furnace) ?: return false
			val prevOverheat = entity.overheatLevel
			
			if (prevOverheat == 0F){
				return false
			}
			
			val newOverheat = prevOverheat * 0.5F
			val reductionRatio = (prevOverheat - newOverheat) / OVERHEAT_LEVEL_LIMIT
			val plateSpeedReduction = reductionRatio * 0.5F
			
			entity.overheatLevel = newOverheat
			
			for(plate in getAttachedPlates(furnace)){
				plate.reduceSpeed(plateSpeedReduction)
			}
			
			PacketClientFX(FX_COOLING, FxCoolingData(furnace.pos, reductionRatio)).sendToAllAround(furnace, 24.0)
			return true
		}
		
		private fun dropOverheatItem(world: World, pos: BlockPos, stack: ItemStack){
			val rand = world.rand
			val (targetX, targetY, targetZ) = pos.center.add(rand.nextFloat(-0.24, 0.24), 0.0, rand.nextFloat(-0.24, 0.24))
			
			while(stack.isNotEmpty){
				EntityItemFreshlyCooked(world, targetX, targetY, targetZ, stack.splitStack(rand.nextInt(10, 20))).apply {
					motionVec = Vec3d.ZERO
					world.spawnEntity(this)
				}
			}
		}
		
		private val PARTICLE_COOLING = ParticleSpawnerVanilla(
			type = SMOKE_NORMAL,
			pos = InBox(0.45F)
		)
		
		private val PARTICLE_OVERHEAT = ParticleSpawnerVanilla(
			type = SMOKE_LARGE,
			pos = Constant(0.25F, UP) + InBox(1.75F, 0.5F, 1.75F)
		)
		
		private val PARTICLE_OVERHEAT_CLOUD = ParticleSpawnerVanilla(
			type = CLOUD,
			pos = Constant(0.25F, UP) + InBox(1.75F, 0.5F, 1.75F)
		)
		
		class FxCoolingData(private val pos: BlockPos, private val amount: Float) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
				writeFloat(amount)
			}
		}
		
		val FX_COOLING = object : IFxHandler<FxCoolingData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val pos = buffer.readPos()
				val amount = buffer.readFloat()
				
				pos.getTile<TileEntityFurnace>(world)?.let(::getAttachedPlates)?.forEach {
					PARTICLE_COOLING.spawn(Point(it.pos, 5 + (10 * amount).floorToInt()), rand)
				}
				
				ModSounds.BLOCK_IGNEOUS_PLATE_COOL.playClient(pos, SoundCategory.BLOCKS, volume = 0.25F + (1.75F * amount), pitch = 0.8F)
			}
		}
		
		val FX_OVERHEAT = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_OVERHEAT.spawn(Point(pos, 50), rand)
				PARTICLE_OVERHEAT_CLOUD.spawn(Point(pos, 15), rand)
				Sounds.ENTITY_GENERIC_EXPLODE.playClient(pos, SoundCategory.BLOCKS, volume = 2F, pitch = 1F)
			}
		}
	}
	
	// Instance
	
	private var extraTicks = 0.0
	private var overheatLevel by EntityData(DATA_OVERHEAT_LEVEL)
	
	init{
		noClip = true
		setSize(0F, 0F)
	}
	
	override fun entityInit(){
		dataManager.register(DATA_OVERHEAT_LEVEL, 0F)
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (!world.isRemote && world.isAreaLoaded(position, 1)){
			val furnace = Pos(this).getTile<TileEntityFurnace>(world)
			
			if (furnace == null){
				setDead()
				return
			}
			
			val plates = getAttachedPlates(furnace)
			
			if (plates.isEmpty()){
				setDead()
				return
			}
			
			val speedMultiplier = (plates.sumByDouble { it.potential } / plates.size) * 2.0 * plates.size.toDouble().pow(0.793)
			increaseFurnaceTicks(furnace, speedMultiplier - 1.0)
			
			if (speedMultiplier < 2.1){
				if (overheatLevel > 0F){
					overheatLevel = max(0F, (overheatLevel - 0.25F) * (if (furnace.isBurning) 0.9996F else 0.998F))
				}
			}
			else if (furnace.isBurning){
				increaseHeatLevel(furnace, (speedMultiplier - 2.2).toFloat())
			}
			else if (plates.any { it.potential > 0.05 }){
				increaseHeatLevel(furnace, ((speedMultiplier - 2.2) * 0.25).toFloat())
			}
		}
	}
	
	// Furnace handling
	
	private fun increaseFurnaceTicks(furnace: TileEntityFurnace, amount: Double){
		extraTicks += max(0.0, amount)
		
		while(extraTicks >= 1.0){
			extraTicks -= 1.0
			
			if (furnace.isBurning){
				val currentCookTime = furnace.getField(FIELD_COOK_TIME_CURRENT)
				val newCookTime = min(currentCookTime + 1, furnace.getField(FIELD_COOK_TIME_TARGET) - 1)
				
				furnace.setField(FIELD_COOK_TIME_CURRENT, newCookTime)
			}
		}
	}
	
	private fun increaseHeatLevel(furnace: TileEntityFurnace, amount: Float){
		overheatLevel += max(0F, amount)
		
		if (overheatLevel > OVERHEAT_LEVEL_LIMIT){
			for(plate in getAttachedPlates(furnace)){
				plate.blastOff()
			}
			
			val pos = Pos(this)
			val freePositions = pos.allInCenteredBox(1, 0, 1).filter { it.isAir(world) }
			
			val ingredientSlots = furnace.getSlotsForFace(UP)
			
			for((slot, stack) in furnace.nonEmptySlots){
				if (slot in ingredientSlots){
					stack.shrink(1)
				}
				
				dropOverheatItem(world, rand.nextItemOrNull(freePositions) ?: pos, stack)
			}
			
			furnace.clear()
			
			if (furnace.javaClass === TileEntityFurnace::class.java){
				repeat(5){
					dropOverheatItem(world, rand.nextItemOrNull(freePositions) ?: pos, ItemStack(Blocks.COBBLESTONE))
				}
			}
			
			PacketClientFX(FX_OVERHEAT, FxBlockData(pos)).sendToAllAround(this, 32.0)
			
			pos.setBlock(world, Blocks.FIRE)
			setDead()
		}
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		setDouble(EXTRA_TICKS_TAG, extraTicks)
		setFloat(OVERHEAT_LEVEL_TAG, overheatLevel)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		extraTicks = getDouble(EXTRA_TICKS_TAG)
		overheatLevel = getFloat(OVERHEAT_LEVEL_TAG)
	}
}
