package chylex.hee.game.entity.projectile
import chylex.hee.game.entity.lookDirVec
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.motionX
import chylex.hee.game.entity.motionZ
import chylex.hee.game.entity.posVec
import chylex.hee.game.particle.ParticleGlitter
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.InSphere
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getState
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.playClient
import chylex.hee.init.ModEntities
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.LerpedDouble
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.scale
import chylex.hee.system.math.square
import chylex.hee.system.math.subtractY
import chylex.hee.system.migration.BlockFlowingFluid
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.readPos
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writePos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type.WORLD_SURFACE
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random
import kotlin.math.sin

class EntityProjectileEyeOfEnder(type: EntityType<EntityProjectileEyeOfEnder>, world: World) : Entity(type, world), IEntityAdditionalSpawnData{
	constructor(thrower: EntityLivingBase, targetPos: BlockPos?) : this(ModEntities.EYE_OF_ENDER, thrower.world){
		this.posVec = thrower.lookPosVec.subtractY(height * 0.5).add(thrower.lookDirVec.scale(1.5))
		this.targetPos = targetPos
	}
	
	private companion object{
		private const val TICK_BEGIN_GLITTER = 30
		private const val TICK_BEGIN_MOVEMENT = 40
		private const val TICK_DROP_NO_TARGET = 50
		
		private const val TICK_FULL_Y_MOVEMENT = 100
		private const val TICK_END_MOVEMENT = 400
		private const val TICK_DESTROY_NO_ENERGY_MIN = 440
		private const val TICK_DESTROY_NO_ENERGY_MAX = TICK_DESTROY_NO_ENERGY_MIN + 100
		
		private const val TICK_REACHED_TARGET_SKIP = 500
		private const val TICK_DROP_REACHED_TARGET = 580
		
		private const val TARGET_TAG = "Target"
		private const val TIMER_TAG = "Timer"
		private const val SPEED_TAG = "Speed"
		
		private val PARTICLE_SMOKE = ParticleSpawnerVanilla(
			type = SMOKE,
			pos = Constant(0.1F, UP) + InBox(0.15F),
			mot = InBox(0.075F),
			ignoreRangeLimit = true,
			hideOnMinimalSetting = false
		)
		
		private val PARTICLE_GLITTER_TICK = ParticleSpawnerCustom(
			type = ParticleGlitter,
			data = ParticleGlitter.Data(color = GlitterColorTick, maxAgeMultiplier = 3..5),
			pos = InBox(0.15F),
			mot = Constant(0.025F, DOWN) + InBox(0.02F)
		)
		
		private val PARTICLE_GLITTER_DESTROY = ParticleSpawnerCustom(
			type = ParticleGlitter,
			data = ParticleGlitter.Data(color = GlitterColorDestroy, maxAgeMultiplier = 1..2),
			pos = InSphere(0.35F),
			mot = InBox(0.04F),
			maxRange = 64.0
		)
		
		private object GlitterColorTick : IRandomColor{
			override fun next(rand: Random): IntColor{
				return if (rand.nextInt(3) == 0)
					RGB(rand.nextInt(76, 128), rand.nextInt(64, 76), rand.nextInt(128, 192))
				else
					RGB(rand.nextInt(51, 76), rand.nextInt(64, 166), rand.nextInt(76, 102))
			}
		}
		
		private object GlitterColorDestroy : IRandomColor{
			override fun next(rand: Random): IntColor{
				return if (rand.nextInt(3) == 0)
					RGB(rand.nextInt(102, 153), rand.nextInt(64, 76), rand.nextInt(153, 216))
				else
					RGB(rand.nextInt(90, 115), rand.nextInt(76, 178), rand.nextInt(102, 128))
			}
		}
		
		private fun shouldFloatAbove(state: BlockState): Boolean{
			return state.material.blocksMovement() || state.block is BlockFlowingFluid
		}
	}
	
	// Instance
	
	val renderBob = LerpedDouble(nextRenderBobOffset)
	
	private val posVecWithOffset
		get() = posVec.addY(0.1 + renderBob.currentValue)
	
	private val nextRenderBobOffset
		get() = 0.35 + (sin(timer * 0.15) * 0.25) // 0.35 offset for bounding box
	
	private val targetVecXZ
		get() = targetPos?.let { Vec3.fromXZ(it.x + 0.5 - posX, it.z + 0.5 - posZ) } ?: Vec3d.ZERO
	
	private var targetPos: BlockPos? = null
	private var timer = 0
	private var speed = 0F
	
	private var prevPos = BlockPos.ZERO
	private var targetY = 0.0
	
	// Initialization
	
	override fun registerData(){}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writePos(targetPos ?: BlockPos.ZERO)
		writeShort(timer)
		writeFloat(speed)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		targetPos = buffer.readPos().takeIf { it != BlockPos.ZERO }
		timer = buffer.readShort().toInt()
		speed = buffer.readFloat()
	}
	
	// Behavior
	
	override fun tick(){
		lastTickPosX = posX
		lastTickPosY = posY
		lastTickPosZ = posZ
		super.tick()
		
		if (ticksExisted == 1){
			motion = targetVecXZ.normalize().scale(0.27)
		}
		
		++timer
		rotationYaw += 5F
		
		if (world.isRemote){
			renderBob.update(nextRenderBobOffset)
			
			if (timer == 1){
				PARTICLE_SMOKE.spawn(Point(posVecWithOffset, 8), rand)
			}
			else if (timer > TICK_BEGIN_GLITTER && targetPos != null){
				PARTICLE_GLITTER_TICK.spawn(Point(posVecWithOffset, 3), rand)
			}
		}
		
		if (targetPos == null){
			if (!world.isRemote && timer > TICK_DROP_NO_TARGET){
				dropEye()
			}
		}
		else if (timer > TICK_BEGIN_MOVEMENT){
			val pos = Pos(this)
			
			if (prevPos != pos){
				prevPos = pos
				updateTargetAltitude()
			}
			
			moveTowardsTarget()
		}
	}
	
	private fun updateTargetAltitude(){
		val perpendicular = Vec3.fromXZ(-(motionZ * 3.0), motionX * 3.0)
		val step = motion.scale(4)
		
		val parallelStarts = arrayOf(
			posVec,
			posVec.subtract(perpendicular),
			posVec.add(perpendicular)
		)
		
		val checkedBlocks = HashSet<BlockPos>(36, 1F)
		
		parallelStarts.flatMapTo(checkedBlocks){
			start -> (0..11).map { world.getHeight(WORLD_SURFACE, Pos(start.add(step.scale(it)))) }
		}
		
		if (checkedBlocks.isEmpty()){
			targetY = posY
			return
		}
		
		val averageY = checkedBlocks
			.map { pos -> 1 + (pos.offsetUntil(DOWN, 0..(pos.y)){ shouldFloatAbove(it.getState(world)) } ?: pos).y }
			.sortedDescending()
			.take(1 + (checkedBlocks.size / 4))
			.average()
		
		targetY = averageY + 2.5
	}
	
	private fun moveTowardsTarget(){
		val ySpeedMp: Float
		
		if (targetVecXZ.lengthSquared() < square(7.0)){
			if (speed > 0F){
				speed -= 0.025F
			}
			
			ySpeedMp = speed
			
			if (timer < TICK_REACHED_TARGET_SKIP){
				timer = TICK_REACHED_TARGET_SKIP
			}
			else if (!world.isRemote && timer > TICK_DROP_REACHED_TARGET){
				dropEye()
			}
		}
		else{
			if (timer <= TICK_END_MOVEMENT && speed < 1F){
				speed += 0.02F
			}
			else if (timer > TICK_END_MOVEMENT && speed > 0.25F){
				speed -= 0.015F
			}
			
			if (speed > 0.7F && targetY - posY > 4.0){
				speed -= 0.05F
			}
			
			ySpeedMp = if (timer < TICK_FULL_Y_MOVEMENT) speed else 1F
			
			if (!world.isRemote && timer > TICK_DESTROY_NO_ENERGY_MIN && timer > rand.nextInt(TICK_DESTROY_NO_ENERGY_MIN, TICK_DESTROY_NO_ENERGY_MAX)){
				remove()
			}
		}
		
		val (newX, _, newZ) = posVec.add(motion.scale(speed))
		val newY = offsetTowards(posY, targetY, 0.03 * ySpeedMp)
		setPosition(newX, newY, newZ)
	}
	
	private fun dropEye(){
		EntityItem(world, posX, posY + nextRenderBobOffset - 0.25, posZ, ItemStack(Items.ENDER_EYE)).apply {
			setDefaultPickupDelay()
			world.addEntity(this)
		}
		
		remove()
	}
	
	override fun remove(){
		super.remove()
		
		if (world.isRemote){
			val pos = posVecWithOffset
			
			PARTICLE_SMOKE.spawn(Point(pos, 18), rand)
			PARTICLE_GLITTER_DESTROY.spawn(Point(pos, 50), rand)
			Sounds.ENTITY_ENDER_EYE_DEATH.playClient(pos, SoundCategory.NEUTRAL)
		}
	}
	
	override fun canBeAttackedWithItem(): Boolean{
		return false
	}
	
	@Sided(Side.CLIENT)
	override fun setPositionAndRotationDirect(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, posRotationIncrements: Int, teleport: Boolean){}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		targetPos?.let { putPos(TARGET_TAG, it) }
		putShort(TIMER_TAG, timer.toShort())
		putFloat(SPEED_TAG, speed)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		targetPos = getPosOrNull(TARGET_TAG)
		timer = getShort(TIMER_TAG).toInt()
		speed = getFloat(SPEED_TAG)
	}
}
