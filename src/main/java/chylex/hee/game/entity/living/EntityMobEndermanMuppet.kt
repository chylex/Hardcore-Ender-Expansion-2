package chylex.hee.game.entity.living

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.MuppetType.FIRST_KILL
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.MuppetType.INVALID
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.entity.util.with
import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.world.World

class EntityMobEndermanMuppet(type: EntityType<EntityMobEndermanMuppet>, world: World) : EntityMobAbstractEnderman(type, world) {
	constructor(world: World, muppetType: MuppetType) : this(ModEntities.ENDERMAN_MUPPET, world) {
		this.muppetType = muppetType
	}
	
	object Type : BaseType<EntityMobEndermanMuppet>() {
		override val localization
			get() = LocalizationStrategy.None
		
		override val classification
			get() = EntityClassification.MISC
		
		override val tracker
			get() = super.tracker.copy(trackingRange = 6)
		
		override val attributes
			get() = DefaultEntityAttributes.hostileMob.with(
				MAX_HEALTH     to 40.0,
				MOVEMENT_SPEED to 0.0,
			)
	}
	
	private companion object {
		private const val MUPPET_TYPE_TAG = "Type"
	}
	
	enum class MuppetType {
		INVALID,
		FIRST_KILL
	}
	
	// Instance
	
	override val teleportCooldown = Int.MAX_VALUE
	private var muppetType = INVALID
	
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
		if (muppetType == INVALID) {
			remove()
		}
		else if (muppetType == FIRST_KILL) {
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
		
		putEnum(MUPPET_TYPE_TAG, this@EntityMobEndermanMuppet.muppetType)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		this@EntityMobEndermanMuppet.muppetType = getEnum<MuppetType>(MUPPET_TYPE_TAG) ?: INVALID
	}
}
