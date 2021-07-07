package chylex.hee.game.entity.living

import chylex.hee.game.entity.living.EntityMobEndermanMuppet.Type.FIRST_KILL
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.Type.INVALID
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class EntityMobEndermanMuppet(type: EntityType<EntityMobEndermanMuppet>, world: World) : EntityMobAbstractEnderman(type, world) {
	constructor(world: World, type: Type) : this(ModEntities.ENDERMAN_MUPPET, world) {
		this.type = type
	}
	
	private companion object {
		private const val TYPE_TAG = "Type"
	}
	
	enum class Type {
		INVALID,
		FIRST_KILL
	}
	
	// Instance
	
	override val teleportCooldown = Int.MAX_VALUE
	private var type = INVALID
	
	init {
		experienceValue = 0
	}
	
	override fun tick() {
		super.tick()
		
		if (ticksExisted == 1) {
			setRenderYawOffset(rotationYawHead)
			prevRenderYawOffset = renderYawOffset
		}
	}
	
	override fun updateAITasks() {
		if (type == INVALID) {
			remove()
		}
		else if (type == FIRST_KILL) {
			if (world.isAreaLoaded(Pos(this), 24) && world.selectExistingEntities.inRange<EntityTechnicalCausatumEvent>(posVec, 24.0).none { it.type == CausatumEventEndermanKill::class.java }) {
				despawnOutOfWorld()
			}
		}
	}
	
	// Despawning
	
	fun despawnOutOfWorld() {
		EndermanTeleportHandler(this).teleportOutOfWorld(force = true)
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean {
		return false
	}
	
	override fun preventDespawn(): Boolean {
		return true
	}
	
	override fun checkDespawn() {
		return
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putEnum(TYPE_TAG, this@EntityMobEndermanMuppet.type)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		this@EntityMobEndermanMuppet.type = getEnum<Type>(TYPE_TAG) ?: INVALID
	}
}
