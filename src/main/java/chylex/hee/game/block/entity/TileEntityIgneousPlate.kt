package chylex.hee.game.block.entity
import chylex.hee.client.model.block.ModelBlockIgneousPlate.ANIMATION_PERIOD
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.AXIS_Y
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.math.LerpedDouble
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.setAir
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class TileEntityIgneousPlate : TileEntityBase(), ITickable{
	private companion object{
		private const val TICKS_TO_HEAT_UP = 1100
		private const val TICKS_TO_COOL_DOWN = 2600
		
		private const val PROGRESS_HEAT_UP_PER_TICK = 1.0 / TICKS_TO_HEAT_UP
		private const val PROGRESS_COOL_DOWN_PER_TICK = 1.0 / TICKS_TO_COOL_DOWN
		
		private fun createPositionOffset(facing: EnumFacing, offset: Float) = if (facing.axis == AXIS_Y)
			InBox(offset, 0F, offset)
		else
			InBox(offset * abs(facing.zOffset), offset, offset * abs(facing.xOffset))
		
		private val PARTICLE_WORK = BlockIgneousPlate.FACING_NOT_DOWN.allowedValues.associateWith {
			ParticleSpawnerCustom(
				type = ParticleSmokeCustom,
				data = ParticleSmokeCustom.Data(scale = 0.66F),
				pos = Constant(0.365F, it.opposite) + createPositionOffset(it, 0.2F),
				mot = Constant(0.075F, it) + Constant(0.004F, UP) + InBox(0.01F, 0.01F, 0.01F)
			)
		}
		
		private val PARTICLE_OVERHEATING = BlockIgneousPlate.FACING_NOT_DOWN.allowedValues.associateWith {
			ParticleSpawnerCustom(
				type = ParticleFlameCustom,
				data = ParticleFlameCustom.Data(maxAge = 10),
				pos = Constant(0.335F, it.opposite) + createPositionOffset(it, 0.15F),
				mot = Constant(0.065F, it) + Constant(0.008F, UP) + InBox(0.0075F, 0.0075F, 0.0075F)
			)
		}
	}
	
	val potential
		get() = progress.pow(1.8)
	
	val isWorking
		get() = progress > 0.0
	
	val isOverheating
		get() = overheatingPercentage > 0F
	
	private val overheatingPercentage
		get() = pos.offset(facing.opposite).getTile<TileEntityFurnace>(world)?.let { EntityTechnicalIgneousPlateLogic.getOverheatingPercentage(it) } ?: 0F
	
	private var facing = DOWN
	private var progress = 0.0
	
	val clientCombinedHeat
		get() = progress.toFloat() + overheatingPercentage
	
	val clientThrustAnimation = LerpedDouble(0.0)
	private var animation = 0.0
	
	fun isAttachedTo(furnace: TileEntityFurnace): Boolean{
		return pos.offset(facing.opposite) == furnace.pos
	}
	
	fun reduceSpeed(amount: Float){
		progress = max(0.0, progress - amount)
	}
	
	fun blastOff(){
		val rand = world.rand
		
		EntityItemFreshlyCooked(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, ItemStack(ModBlocks.IGNEOUS_PLATE)).apply {
			motionVec = Vec3d(facing.directionVec).scale(rand.nextFloat(2.5, 3.0)).add(rand.nextFloat(-0.2, 0.2), rand.nextFloat(0.1, 0.2), rand.nextFloat(-0.2, 0.2))
			world.spawnEntity(this)
		}
		
		pos.setAir(world)
	}
	
	override fun firstTick(){
		facing = pos.getState(world)[BlockIgneousPlate.FACING_NOT_DOWN]
	}
	
	override fun update(){
		if (!world.isAreaLoaded(pos, 1)){
			return
		}
		
		val furnace = pos.offset(facing.opposite).getTile<TileEntityFurnace>(world) ?: return
		
		val isBurning = if (world.isRemote)
			furnace.blockType === Blocks.LIT_FURNACE
		else
			furnace.isBurning
		
		if (isBurning){
			progress = min(1.0, progress + PROGRESS_HEAT_UP_PER_TICK)
		}
		else if (potential > 0.0){
			progress = max(0.0, progress - PROGRESS_COOL_DOWN_PER_TICK)
		}
		
		if (world.isRemote){
			val potential = potential
			val threshold = if (isBurning) 0.001 else 0.03
			
			var prevThrustAnim = clientThrustAnimation.currentValue
			
			if (potential > threshold){
				val step = potential * 0.12
				animation += step
				
				if (animation >= 1.0){
					animation -= 1.0
					// TODO sound
					
					PARTICLE_WORK[facing]?.spawn(Point(pos, (2 + (potential * 8)).floorToInt()), world.rand)
					
					val overheatingLevel = EntityTechnicalIgneousPlateLogic.getOverheatingPercentage(furnace)
					
					if (overheatingLevel > 0.0){
						PARTICLE_OVERHEATING[facing]?.spawn(Point(pos, 1 + (max(0F, overheatingLevel - 0.125F) * 4F).floorToInt()), world.rand)
					}
				}
				
				if (prevThrustAnim >= ANIMATION_PERIOD){
					prevThrustAnim -= ANIMATION_PERIOD
					clientThrustAnimation.updateImmediately(prevThrustAnim)
				}
				
				clientThrustAnimation.update(prevThrustAnim + (step * ANIMATION_PERIOD))
			}
			else{
				animation = 0.0
				
				if (prevThrustAnim > ANIMATION_PERIOD / 2){
					prevThrustAnim = ANIMATION_PERIOD - prevThrustAnim
					clientThrustAnimation.updateImmediately(prevThrustAnim)
				}
				
				if (prevThrustAnim > 0.0){
					clientThrustAnimation.update(max(0.0, (prevThrustAnim * 0.95) - 0.005))
				}
			}
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		setDouble("Progress", progress)
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		progress = getDouble("Progress")
	}
}
