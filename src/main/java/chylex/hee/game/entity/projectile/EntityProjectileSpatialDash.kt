package chylex.hee.game.entity.projectile
import chylex.hee.HEE
import chylex.hee.client.sound.MovingSoundSpatialDash
import chylex.hee.client.util.MC
import chylex.hee.game.entity.util.SerializedEntity
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.Teleporter
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.color.IRandomColor.Companion.IRandomColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.scale
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.writeCompactVec
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.IProjectile
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.math.RayTraceResult.Type.MISS
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory
import java.util.Random

class EntityProjectileSpatialDash : Entity, IProjectile{
	companion object{
		private const val PROJECTILE_SPEED_BASE = 1.5F
		private const val PROJECTILE_DISTANCE_BASE = 32 // max distance is 98 blocks
		
		private const val OWNER_TAG = "Owner"
		private const val LIFESPAN_TAG = "Lifespan"
		private const val RANGE_TAG = "Range"
		
		private val TELEPORT_OFFSETS: Array<BlockPos>
		private val TELEPORT = Teleporter(causedInstability = 15u)
		
		private val PARTICLE_TICK = ParticleSpawnerCustom(
			type = ParticleFadingSpot,
			data = ParticleFadingSpot.Data(color = IRandomColor { RGB(nextInt(30, 70), nextInt(70, 130), nextInt(100, 200)) }, lifespan = 20..25, scale = (0.1F)..(0.12F)),
			pos = InBox(0.2F),
			mot = Gaussian(0.008F),
			maxRange = 64.0
		)
		
		private val PARTICLE_EXPIRE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = RGB(240u), scale = 3F),
			pos = InBox(0.6F),
			mot = Gaussian(0.03F),
			maxRange = 128.0
		)
		
		class FxExpireData(private val point: Vec3d, private val ownerEntity: Entity?) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writeCompactVec(point)
				writeInt(ownerEntity?.entityId ?: -1)
			}
		}
		
		val FX_EXPIRE = object : IFxHandler<FxExpireData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				val playerPos = player.posVec
				
				val point = readCompactVec()
				val forceAudible = readInt() == player.entityId
				
				val soundPoint = if (forceAudible){
					val distance = playerPos.distanceTo(point)
					
					if (distance < 10.0){
						point
					}
					else{
						playerPos.add(playerPos.directionTowards(point).scale(10.0 + (distance - 10.0) * 0.04)) // makes the sound audible even at max distance of ~100 blocks
					}
				}
				else{
					point
				}
				
				PARTICLE_EXPIRE.spawn(Point(point, 10), rand)
				ModSounds.ENTITY_SPATIAL_DASH_EXPIRE.playClient(soundPoint, SoundCategory.PLAYERS)
			}
		}
		
		init{
			val offsets = mutableListOf<BlockPos>()
			
			offsets.add(Pos(0, 2, 0))
			offsets.add(Pos(0, 1, 0))
			offsets.addAll(BlockPos.getAllInBox(Pos(-1, -1, -1), Pos(1, 0, 1)))
			offsets.add(Pos(0, -2, 0))
			
			TELEPORT_OFFSETS = offsets.toTypedArray()
		}
		
		private fun canTeleportPlayerOnTop(pos: BlockPos, world: World): Boolean{
			return pos.blocksMovement(world) && !pos.up().blocksMovement(world) && !pos.up(2).blocksMovement(world)
		}
		
		private fun handleBlockHit(entity: EntityLivingBase, hit: Vec3d, motion: Vec3d, pos: BlockPos){
			if (canTeleportPlayerOnTop(pos, entity.world)){
				TELEPORT.toBlock(entity, pos.up())
			}
			else if (!(entity is EntityPlayer && BlockEditor.canBreak(pos, entity))){
				handleGenericHit(entity, hit, motion)
			}
			else if (!teleportEntityNear(entity, hit.add(motion.normalize().scale(1.74)), false)){
				handleGenericHit(entity, hit, motion)
			}
		}
		
		private fun handleGenericHit(entity: EntityLivingBase, hit: Vec3d, motion: Vec3d){
			teleportEntityNear(entity, hit.add(motion.normalize().scale(-1.26)), true)
		}
		
		private fun teleportEntityNear(entity: EntityLivingBase, target: Vec3d, fallback: Boolean): Boolean{
			val world = entity.world
			
			val finalBlock = TELEPORT_OFFSETS
				.map { Pos(target.add(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())) }
				.sortedBy { it.distanceSqTo(target) }
				.firstOrNull { canTeleportPlayerOnTop(it, world) }
			
			if (finalBlock != null){
				TELEPORT.toBlock(entity, finalBlock.up())
				return true
			}
			else if (fallback){
				TELEPORT.toLocation(entity, target)
				return true
			}
			
			return false
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world){
		this.owner = SerializedEntity()
		this.lifespan = 0
		this.range = 0F
	}
	
	constructor(world: World, owner: EntityLivingBase, speedMp: Float, distanceMp: Float) : super(world){
		this.owner = SerializedEntity(owner)
		this.setPosition(owner.posX, owner.posY + owner.eyeHeight - 0.1, owner.posZ)
		
		val realSpeed = PROJECTILE_SPEED_BASE * speedMp
		val realDistance = PROJECTILE_DISTANCE_BASE * distanceMp
		
		val (dirX, dirY, dirZ) = Vec3d.fromPitchYaw(owner.rotationPitch, owner.rotationYaw)
		shoot(dirX, dirY, dirZ, realSpeed, 0F)
		
		this.lifespan = (realDistance / realSpeed).ceilToInt().toShort()
		this.range = realDistance
	}
	
	private var owner: SerializedEntity
	private var lifespan: Short
	private var range: Float
	
	private val cappedMotionVec: Vec3d
		get(){
			val currentMot = motionVec
			
			return if (currentMot.length() <= range)
				currentMot
			else
				currentMot.normalize().scale(range)
		}
	
	init{
		noClip = true
		setSize(0.2F, 0.2F)
	}
	
	override fun entityInit(){}
	
	override fun shoot(dirX: Double, dirY: Double, dirZ: Double, velocity: Float, inaccuracy: Float){
		this.motionVec = Vec3d(dirX, dirY, dirZ).normalize().scale(velocity)
	}
	
	override fun onUpdate(){
		if (world.isRemote){
			if (ticksExisted == 1){
				MC.instance.soundHandler.playSound(MovingSoundSpatialDash(this))
			}
			else{
				PARTICLE_TICK.spawn(Line(Vec3d(prevPosX, prevPosY, prevPosZ), posVec, 0.75), rand)
			}
		}
		
		super.onUpdate()
		
		if (!world.isRemote){
			val hitObject = determineHitObject()
			
			if (hitObject != null && hitObject.typeOfHit != MISS){
				val ownerEntity = owner.get(world)
				
				if (ownerEntity is EntityLivingBase && ownerEntity.world === world){
					if (hitObject.typeOfHit == BLOCK){
						handleBlockHit(ownerEntity, hitObject.hitVec, motionVec, hitObject.blockPos)
					}
					else{
						handleGenericHit(ownerEntity, hitObject.hitVec, motionVec)
					}
				}
				
				setDead()
				return
			}
			
			if (--lifespan <= 0){
				setExpired()
				return
			}
			
			range -= motionVec.length().toFloat()
		}
		
		move(SELF, motionX, motionY, motionZ)
	}
	
	private fun determineHitObject(): RayTraceResult?{
		val currentPos = posVec
		val nextPos = currentPos.add(cappedMotionVec)
		
		val blockResult = world.rayTraceBlocks(currentPos, nextPos)
		
		val ownerEntity = owner.get(world)
		val tracedNextPos = blockResult?.hitVec ?: nextPos
		
		val entityResult = world
			.selectEntities
			.allInBox(entityBoundingBox.expand(motionX, motionY, motionZ).grow(1.0))
			.filter { it.canBeCollidedWith() && it != ownerEntity } // the projectile itself cannot collide with anything, therefore not even itself
			.mapNotNull { it.entityBoundingBox.grow(0.3).calculateIntercept(currentPos, tracedNextPos) }
			.minBy { currentPos.squareDistanceTo(it.hitVec) }
			?.let { RayTraceResult(it.entityHit, it.hitVec) } // EntityThrowable sets hitVec to the middle of the entity, but that wouldn't work too nicely here
		
		return (entityResult ?: blockResult)?.takeUnless { ForgeEventFactory.onProjectileImpact(this, it) }
	}
	
	private fun setExpired(){
		val ownerEntity = owner.get(world)
		val expirePos = posVec.add(cappedMotionVec)
		
		PacketClientFX(FX_EXPIRE, FxExpireData(expirePos, ownerEntity)).let {
			it.sendToAllAround(this, 32.0)
			
			if (ownerEntity is EntityPlayer && expirePos.squareDistanceTo(ownerEntity.posVec) > square(32)){
				it.sendToPlayer(ownerEntity)
			}
		}
		
		setDead()
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		owner.writeToNBT(this, OWNER_TAG)
		setShort(LIFESPAN_TAG, lifespan)
		setFloat(RANGE_TAG, range)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		owner.readFromNBT(this, OWNER_TAG)
		lifespan = getShort(LIFESPAN_TAG)
		range = getFloat(RANGE_TAG)
	}
}
