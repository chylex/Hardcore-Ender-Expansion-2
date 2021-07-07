package chylex.hee.game.entity.projectile

import chylex.hee.HEE
import chylex.hee.game.block.properties.BlockBuilder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.PHASING
import chylex.hee.game.item.infusion.Infusion.RIDING
import chylex.hee.game.item.infusion.Infusion.SLOW
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.world.util.getBlocksInside
import chylex.hee.game.world.util.isAir
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.buffer.use
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Pos
import chylex.hee.util.math.subtractY
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.EnderPearlEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks

class EntityProjectileEnderPearl(type: EntityType<EntityProjectileEnderPearl>, world: World) : EnderPearlEntity(type, world), IEntityAdditionalSpawnData {
	constructor(thrower: Entity, infusions: InfusionList) : this(ModEntities.ENDER_PEARL, thrower.world) {
		shooter = thrower
		loadInfusions(infusions)
		setPosition(thrower.posX, thrower.posY + thrower.eyeHeight - 0.1F, thrower.posZ)
		setDirectionAndMovement(thrower, thrower.rotationPitch, thrower.rotationYaw, 0F, 1.5F, 1F)
	}
	
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private val DAMAGE_HIT_ENTITY = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
		
		class RayTraceIndestructible(startVec: Vector3d, endVec: Vector3d, entity: Entity) : RayTraceContext(startVec, endVec, BlockMode.COLLIDER, FluidMode.NONE, entity) {
			override fun getBlockShape(state: BlockState, world: IBlockReader, pos: BlockPos): VoxelShape {
				return if (state.getBlockHardness(world, pos) == INDESTRUCTIBLE_HARDNESS)
					VoxelShapes.fullCube()
				else
					VoxelShapes.empty()
			}
		}
		
		private const val HAS_PHASED_TAG = "HasPhased"
		
		@SubscribeEvent
		fun onEntityJoinWorld(e: EntityJoinWorldEvent) {
			val original = e.entity
			
			if (original is EnderPearlEntity && original !is EntityProjectileEnderPearl) {
				e.isCanceled = true
				e.world.addEntity(EntityProjectileEnderPearl(original.shooter!!, InfusionList.EMPTY))
			}
		}
		
		@SubscribeEvent(EventPriority.LOWEST)
		fun onLivingAttack(e: LivingAttackEvent) {
			if (e.source === DamageSource.IN_WALL && !e.entity.world.isRemote) {
				val riding = e.entityLiving.ridingEntity
				
				if (riding is EntityProjectileEnderPearl && riding.infusions.has(HARMLESS)) {
					e.isCanceled = true
				}
			}
		}
	}
	
	private var infusions = InfusionList.EMPTY
	private var hasPhasedIntoWall = false
	private var hasPhasingFinished = false
	
	// Initialization
	
	private fun loadInfusions(infusions: InfusionList) {
		this.infusions = infusions
		this.noClip = infusions.has(PHASING)
		
		if (infusions.has(RIDING)) {
			shooter?.startRiding(this, true)
		}
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeBoolean(infusions.has(HARMLESS))
		writeBoolean(infusions.has(SLOW))
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		var list = InfusionList.EMPTY
		
		if (readBoolean()) {
			list = list.with(HARMLESS)
		}
		
		if (readBoolean()) {
			list = list.with(SLOW)
		}
		
		loadInfusions(list)
	}
	
	override fun shoot(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) {
		super.shoot(x, y, z, velocity, inaccuracy)
		
		if (infusions.has(SLOW)) {
			motion = motion.scale(0.1)
		}
	}
	
	// Behavior
	
	override fun tick() {
		val prevPos = posVec
		val prevMot = motion
		super.tick()
		
		if (infusions.has(SLOW)) {
			motion = prevMot.scale(0.999).subtractY(gravityVelocity * 0.01)
		}
		else if (!hasNoGravity()) {
			motion = prevMot.scale(0.99).subtractY(gravityVelocity.toDouble())
		}
		
		if (!world.isRemote && infusions.has(PHASING) && hasPhasedIntoWall) {
			if (world.rayTraceBlocks(RayTraceIndestructible(prevPos, prevPos.add(prevMot), this)).type == Type.BLOCK) {
				hasPhasingFinished = true
				posVec = prevPos
			}
			else if (boundingBox.grow(0.15, 0.15, 0.15).getBlocksInside().all { it.isAir(world) }) {
				hasPhasingFinished = true
			}
			
			if (hasPhasingFinished) {
				onImpact(BlockRayTraceResult.createMiss(posVec, DOWN, Pos(this)))
			}
		}
	}
	
	override fun onImpact(result: RayTraceResult) {
		if (infusions.has(PHASING) && !hasPhasingFinished) {
			hasPhasedIntoWall = true
			return
		}
		
		val thrower: Entity? = shooter
		val hitEntity: Entity? = (result as? EntityRayTraceResult)?.entity
		
		if (hitEntity != null && hitEntity === thrower) {
			return
		}
		
		if (hitEntity != null && !infusions.has(HARMLESS)) {
			DAMAGE_HIT_ENTITY.dealToIndirectly(4F, hitEntity, this, thrower)
		}
		
		if (!world.isRemote) {
			remove()
			
			val damage = if (infusions.has(HARMLESS)) 0F else 1F + world.difficulty.id
			val teleport = Teleporter(damageDealt = damage, causedInstability = 20u)
			
			if (thrower is ServerPlayerEntity) {
				if (thrower.connection.networkManager.isChannelOpen && thrower.world === world) {
					teleport.toLocation(thrower, posVec)
				}
			}
			else if (thrower is LivingEntity) {
				teleport.toLocation(thrower, posVec)
			}
		}
	}
	
	override fun removePassenger(passenger: Entity) {
		super.removePassenger(passenger)
		
		if (passenger === shooter && isAlive) {
			onImpact(BlockRayTraceResult.createMiss(posVec, UP, Pos(this)))
		}
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		InfusionTag.setList(this, infusions)
		putBoolean(HAS_PHASED_TAG, hasPhasedIntoWall)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		loadInfusions(InfusionTag.getList(this))
		hasPhasedIntoWall = getBoolean(HAS_PHASED_TAG)
	}
}
