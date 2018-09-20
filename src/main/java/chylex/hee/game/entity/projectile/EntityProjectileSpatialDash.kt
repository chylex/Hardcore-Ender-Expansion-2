package chylex.hee.game.entity.projectile
import chylex.hee.game.entity.utils.SerializedEntity
import chylex.hee.game.item.util.BlockEditor
import chylex.hee.system.util.Pos
import chylex.hee.system.util.add
import chylex.hee.system.util.center
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.toRadians
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.IProjectile
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.math.RayTraceResult.Type.MISS
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import kotlin.math.cos
import kotlin.math.sin

class EntityProjectileSpatialDash : Entity, IProjectile{
	private companion object{
		private const val PROJECTILE_SPEED = 1.5F
		private const val PROJECTILE_DISTANCE = 32
		
		private val TELEPORT_OFFSETS: Array<BlockPos>
		
		init{
			val offsets = mutableListOf<BlockPos>()
			
			offsets.add(Pos(0, 2, 0))
			offsets.add(Pos(0, 1, 0))
			offsets.addAll(BlockPos.getAllInBox(Pos(-1, -1, -1), Pos(1, 0, 1)))
			offsets.add(Pos(0, -2, 0))
			
			TELEPORT_OFFSETS = offsets.toTypedArray()
		}
		
		private fun canTeleportPlayerOnTop(pos: BlockPos, world: World): Boolean{
			return pos.getMaterial(world).blocksMovement() && !pos.up().getMaterial(world).blocksMovement() && !pos.up(2).getMaterial(world).blocksMovement()
		}
		
		private fun handleBlockHit(entity: EntityLivingBase, hit: Vec3d, motion: Vec3d, pos: BlockPos){
			if (canTeleportPlayerOnTop(pos, entity.world)){
				teleportEntityTo(entity, pos.center.add(0.0, 0.51, 0.0))
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
			
			if (finalBlock == null && !fallback){
				return false
			}
			
			teleportEntityTo(entity, finalBlock?.center?.add(0.0, 0.51, 0.0) ?: target)
			return true
		}
		
		private fun teleportEntityTo(entity: EntityLivingBase, position: Vec3d){
			val event = EnderTeleportEvent(entity, position.x, position.y, position.z, 0F)
			
			if (!MinecraftForge.EVENT_BUS.post(event)){
				if (entity.isRiding){
					entity.dismountRidingEntity()
				}
				
				if (entity.isPlayerSleeping && entity is EntityPlayer){
					entity.wakeUpPlayer(true, true, false)
				}
				
				entity.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ)
				entity.fallDistance = 0F
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world){
		this.owner = SerializedEntity()
		this.lifespan = 0
	}
	
	constructor(world: World, owner: EntityLivingBase) : super(world){
		this.owner = SerializedEntity(owner)
		this.setPosition(owner.posX, owner.posY + owner.eyeHeight - 0.1, owner.posZ)
		
		val dirX = -sin(owner.rotationYaw.toRadians()) * cos(owner.rotationPitch.toRadians())
		val dirY = -sin(owner.rotationPitch.toRadians())
		val dirZ = cos(owner.rotationYaw.toRadians()) * cos(owner.rotationPitch.toRadians())
		
		shoot(dirX, dirY, dirZ, PROJECTILE_SPEED, 0F)
		
		this.lifespan = (PROJECTILE_DISTANCE / PROJECTILE_SPEED).floorToInt().toShort()
	}
	
	private var owner: SerializedEntity
	private var lifespan: Short
	
	init{
		noClip = true
		setSize(0.2F, 0.2F)
	}
	
	override fun entityInit(){}
	
	override fun shoot(dirX: Double, dirY: Double, dirZ: Double, velocity: Float, inaccuracy: Float){
		this.motionVec = Vec3d(dirX, dirY, dirZ).normalize().scale(velocity.toDouble())
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (world.isRemote){
			// TODO fx & hide default model
		}
		else{
			val hitObject = determineHitObject()
			
			if (hitObject != null && hitObject.typeOfHit != MISS){
				val ownerEntity = owner.get(world)
				
				if (ownerEntity is EntityLivingBase && ownerEntity.world == world){
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
				setDead()
				return
			}
		}
		
		move(SELF, motionX, motionY, motionZ)
	}
	
	private fun determineHitObject(): RayTraceResult?{
		val currentPos = posVec
		val nextPos = currentPos.add(motionVec)
		
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
	
	override fun writeEntityToNBT(nbt: NBTTagCompound){
		with(this.heeTag(nbt)){
			owner.writeToNBT(this, "Owner")
			setShort("Lifespan", lifespan)
		}
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound){
		with(this.heeTag(nbt)){
			owner.readFromNBT(this, "Owner")
			lifespan = getShort("Lifespan")
		}
	}
}
