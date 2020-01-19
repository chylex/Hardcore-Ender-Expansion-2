package chylex.hee.game.block.entity
import chylex.hee.client.model.block.ModelBlockIgneousPlate.ANIMATION_PERIOD
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.entity.base.TileEntityBaseSpecialFirstTick
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModTileEntities
import chylex.hee.system.migration.Facing.AXIS_Y
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.BlockFurnace
import chylex.hee.system.migration.vanilla.TileEntityFurnace
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.center
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.math.LerpedDouble
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.removeBlock
import chylex.hee.system.util.use
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class TileEntityIgneousPlate(type: TileEntityType<TileEntityIgneousPlate>) : TileEntityBaseSpecialFirstTick(type){
	constructor() : this(ModTileEntities.IGNEOUS_PLATE)
	
	private companion object{
		private const val TICKS_TO_HEAT_UP = 1100
		private const val TICKS_TO_COOL_DOWN = 2600
		
		private const val PROGRESS_HEAT_UP_PER_TICK = 1.0 / TICKS_TO_HEAT_UP
		private const val PROGRESS_COOL_DOWN_PER_TICK = 1.0 / TICKS_TO_COOL_DOWN
		
		private const val PROGRESS_TAG = "Progress"
		
		private fun createPositionOffset(facing: Direction, offset: Float) = if (facing.axis == AXIS_Y)
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
		get() = pos.offset(facing.opposite).getTile<TileEntityFurnace>(wrld)?.let { EntityTechnicalIgneousPlateLogic.getOverheatingPercentage(it) } ?: 0F
	
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
		markDirty()
	}
	
	fun blastOff(){
		val rand = wrld.rand
		
		EntityItemFreshlyCooked(wrld, pos.center, ItemStack(ModBlocks.IGNEOUS_PLATE)).apply {
			motion = Vec3d(facing.directionVec).scale(rand.nextFloat(2.5, 3.0)).add(rand.nextFloat(-0.2, 0.2), rand.nextFloat(0.1, 0.2), rand.nextFloat(-0.2, 0.2))
			wrld.addEntity(this)
		}
		
		pos.removeBlock(wrld)
	}
	
	override fun firstTick(){
		facing = pos.getState(wrld)[BlockIgneousPlate.FACING_NOT_DOWN]
	}
	
	override fun tick(){
		super.tick()
		
		if (!wrld.isAreaLoaded(pos, 1)){
			return
		}
		
		val furnace = pos.offset(facing.opposite).getTile<TileEntityFurnace>(wrld) ?: return
		
		val isBurning = if (wrld.isRemote)
			furnace.blockState[BlockFurnace.LIT]
		else
			furnace.isBurning
		
		if (isBurning){
			if (progress < 1.0){
				progress = min(1.0, progress + PROGRESS_HEAT_UP_PER_TICK)
				markDirty()
			}
		}
		else if (potential > 0.0){
			progress = max(0.0, progress - PROGRESS_COOL_DOWN_PER_TICK)
			markDirty()
		}
		
		if (wrld.isRemote){
			val potential = potential
			val threshold = if (isBurning) 0.001 else 0.03
			
			var prevThrustAnim = clientThrustAnimation.currentValue
			
			if (potential > threshold){
				val step = potential * 0.12
				animation += step
				
				if (animation >= 1.0){
					animation -= 1.0
					// TODO sound
					
					PARTICLE_WORK[facing]?.spawn(Point(pos, (2 + (potential * 8)).floorToInt()), wrld.rand)
					
					val overheatingLevel = EntityTechnicalIgneousPlateLogic.getOverheatingPercentage(furnace)
					
					if (overheatingLevel > 0.0){
						PARTICLE_OVERHEATING[facing]?.spawn(Point(pos, 1 + (max(0F, overheatingLevel - 0.125F) * 4F).floorToInt()), wrld.rand)
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
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		putDouble(PROGRESS_TAG, progress)
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		progress = getDouble(PROGRESS_TAG)
	}
}
