package chylex.hee.game.entity.projectile

import chylex.hee.HEE
import chylex.hee.client.MC
import chylex.hee.client.sound.MovingSoundSpatialDash
import chylex.hee.game.entity.SerializedEntity
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.posVec
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.Pos
import chylex.hee.game.world.blocksMovement
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.playClient
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.IFxData
import chylex.hee.network.fx.IFxHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.math.Vec
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.scale
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.readCompactVec
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeCompactVec
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.IProjectile
import net.minecraft.entity.MoverType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.projectile.ProjectileHelper
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random

class EntityProjectileSpatialDash(type: EntityType<EntityProjectileSpatialDash>, world: World) : Entity(type, world), IProjectile {
	constructor(world: World, owner: EntityLivingBase, speedMp: Float, distanceMp: Float) : this(ModEntities.SPATIAL_DASH, world) {
		this.owner = SerializedEntity(owner)
		this.setPosition(owner.posX, owner.posY + owner.eyeHeight - 0.1, owner.posZ)
		
		val realSpeed = PROJECTILE_SPEED_BASE * speedMp
		val realDistance = PROJECTILE_DISTANCE_BASE * distanceMp
		
		val (dirX, dirY, dirZ) = Vec3d.fromPitchYaw(owner.rotationPitch, owner.rotationYaw)
		shoot(dirX, dirY, dirZ, realSpeed, 0F)
		
		this.lifespan = (realDistance / realSpeed).ceilToInt().toShort()
		this.range = realDistance
	}
	
	companion object {
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
		
		class FxExpireData(private val point: Vec3d, private val ownerEntity: Entity?) : IFxData {
			override fun write(buffer: PacketBuffer) = buffer.use {
				writeCompactVec(point)
				writeInt(ownerEntity?.entityId ?: -1)
			}
		}
		
		val FX_EXPIRE = object : IFxHandler<FxExpireData> {
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				val playerPos = player.posVec
				
				val point = readCompactVec()
				val forceAudible = readInt() == player.entityId
				
				val soundPoint = if (forceAudible) {
					val distance = playerPos.distanceTo(point)
					
					if (distance < 10.0) {
						point
					}
					else {
						playerPos.add(playerPos.directionTowards(point).scale(offsetTowards(10.0, distance, 0.04))) // makes the sound audible even at max distance of ~100 blocks
					}
				}
				else {
					point
				}
				
				PARTICLE_EXPIRE.spawn(Point(point, 10), rand)
				ModSounds.ENTITY_SPATIAL_DASH_EXPIRE.playClient(soundPoint, SoundCategory.PLAYERS)
			}
		}
		
		init {
			val offsets = mutableListOf<BlockPos>()
			
			offsets.add(Pos(0, 2, 0))
			offsets.add(Pos(0, 1, 0))
			
			for(pos in BlockPos.getAllInBox(Pos(-1, -1, -1), Pos(1, 0, 1))) {
				offsets.add(pos)
			}
			
			offsets.add(Pos(0, -2, 0))
			
			TELEPORT_OFFSETS = offsets.toTypedArray()
		}
		
		private fun canTeleportPlayerOnTop(pos: BlockPos, world: World): Boolean {
			return pos.blocksMovement(world) && !pos.up().blocksMovement(world) && !pos.up(2).blocksMovement(world)
		}
		
		private fun handleBlockHit(entity: EntityLivingBase, hit: Vec3d, motion: Vec3d, pos: BlockPos) {
			if (canTeleportPlayerOnTop(pos, entity.world)) {
				TELEPORT.toBlock(entity, pos.up())
			}
			else if (!(entity is EntityPlayer && BlockEditor.canBreak(pos, entity))) {
				handleGenericHit(entity, hit, motion)
			}
			else if (!teleportEntityNear(entity, hit.add(motion.normalize().scale(1.74)), false)) {
				handleGenericHit(entity, hit, motion)
			}
		}
		
		private fun handleGenericHit(entity: EntityLivingBase, hit: Vec3d, motion: Vec3d) {
			teleportEntityNear(entity, hit.add(motion.normalize().scale(-1.26)), true)
		}
		
		private fun teleportEntityNear(entity: EntityLivingBase, target: Vec3d, fallback: Boolean): Boolean {
			val world = entity.world
			
			val finalBlock = TELEPORT_OFFSETS
				.map { Pos(target.add(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())) }
				.sortedBy { it.distanceSqTo(target) }
				.firstOrNull { canTeleportPlayerOnTop(it, world) }
			
			if (finalBlock != null) {
				TELEPORT.toBlock(entity, finalBlock.up())
				return true
			}
			else if (fallback) {
				TELEPORT.toLocation(entity, target)
				return true
			}
			
			return false
		}
	}
	
	private var owner = SerializedEntity()
	private var lifespan: Short = 0
	private var range = 0F
	
	private val cappedMotionVec: Vec3d
		get() {
			return if (motion.length() <= range)
				motion
			else
				motion.normalize().scale(range)
		}
	
	init {
		noClip = true
	}
	
	override fun registerData() {}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun shoot(dirX: Double, dirY: Double, dirZ: Double, velocity: Float, inaccuracy: Float) {
		this.motion = Vec(dirX, dirY, dirZ).normalize().scale(velocity)
	}
	
	override fun move(type: MoverType, pos: Vec3d) {
		super.move(type, pos)
		
		if (type == SELF && world.isRemote) {
			PARTICLE_TICK.spawn(Line(Vec(prevPosX, prevPosY, prevPosZ), posVec, 0.75), rand)
		}
	}
	
	override fun tick() {
		if (world.isRemote && ticksExisted == 1) {
			MC.instance.soundHandler.play(MovingSoundSpatialDash(this))
		}
		
		super.tick()
		
		if (!world.isRemote) {
			val hitObject = determineHitObject()
			
			if (hitObject != null && hitObject.type != Type.MISS) {
				val ownerEntity = owner.get(world)
				
				if (ownerEntity is EntityLivingBase && ownerEntity.world === world) {
					if (hitObject is BlockRayTraceResult) {
						handleBlockHit(ownerEntity, hitObject.hitVec, motion, hitObject.pos)
					}
					else {
						handleGenericHit(ownerEntity, hitObject.hitVec, motion)
					}
				}
				
				remove()
				return
			}
			
			if (--lifespan <= 0) {
				setExpired()
				return
			}
			
			range -= motion.length().toFloat()
		}
		
		move(SELF, motion)
	}
	
	private fun determineHitObject(): RayTraceResult? {
		val currentPos = posVec
		val nextPos = currentPos.add(cappedMotionVec)
		
		val blockResult = world.rayTraceBlocks(RayTraceContext(currentPos, nextPos, BlockMode.COLLIDER, FluidMode.NONE, this)).takeIf { it.type == Type.BLOCK }
		
		val ownerEntity = owner.get(world)
		val tracedNextPos = blockResult?.hitVec ?: nextPos
		
		val entityResult = ProjectileHelper.rayTraceEntities(world, this, currentPos, nextPos, boundingBox.expand(motion).grow(1.0)) {
			// the projectile itself cannot collide with anything, therefore not even itself
			it.canBeCollidedWith() && !it.isSpectator && it !== ownerEntity
		}?.let {
			// ProjectileHelper sets hitVec to the bottom of the entity, but that wouldn't work too nicely here
			it.entity.boundingBox.grow(0.3).rayTrace(currentPos, tracedNextPos).orElse(null)?.let { hit -> EntityRayTraceResult(it.entity, hit) }
		}
		
		return (entityResult ?: blockResult)?.takeUnless { ForgeEventFactory.onProjectileImpact(this, it) }
	}
	
	private fun setExpired() {
		val ownerEntity = owner.get(world)
		val expirePos = posVec.add(cappedMotionVec)
		
		PacketClientFX(FX_EXPIRE, FxExpireData(expirePos, ownerEntity)).let {
			it.sendToAllAround(this, 32.0)
			
			if (ownerEntity is EntityPlayer && expirePos.squareDistanceTo(ownerEntity.posVec) > square(32)) {
				it.sendToPlayer(ownerEntity)
			}
		}
		
		remove()
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		owner.writeToNBT(this, OWNER_TAG)
		putShort(LIFESPAN_TAG, lifespan)
		putFloat(RANGE_TAG, range)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		owner.readFromNBT(this, OWNER_TAG)
		lifespan = getShort(LIFESPAN_TAG)
		range = getFloat(RANGE_TAG)
	}
}
