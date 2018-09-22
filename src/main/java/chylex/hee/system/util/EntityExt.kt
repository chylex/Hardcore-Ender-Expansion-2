package chylex.hee.system.util
import chylex.hee.game.entity.util.EntitySelector
import com.google.common.base.Predicates
import net.minecraft.enchantment.EnchantmentProtection
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.EntitySelectors
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

// Properties

var Entity.posVec
	get() = this.positionVector
	set(value){
		this.posX = value.x
		this.posY = value.y
		this.posZ = value.z
	}

var Entity.motionVec
	get() = Vec3d(this.motionX, this.motionY, this.motionZ)
	set(value){
		this.motionX = value.x
		this.motionY = value.y
		this.motionZ = value.z
	}

val Entity.lookVec
	get() = this.getLook(0F)

// Methods

fun Entity.setFireTicks(ticks: Int){
	val prevFire = this.fire
	this.setFire(ticks / 20) // in case something overrides it
	
	val finalTicks = when{
		this is EntityLivingBase -> EnchantmentProtection.getFireTimeForEntity(this, ticks)
		else -> ticks
	}
	
	if (finalTicks > prevFire){
		this.fire = finalTicks
	}
}

fun EntityItem.cloneFrom(source: Entity){
	motionX = source.motionX
	motionY = source.motionY
	motionZ = source.motionZ
	isAirBorne = source.isAirBorne
	
	if (source is EntityItem){
		lifespan = source.lifespan
		pickupDelay = source.pickupDelay
		
		thrower = source.thrower
		owner = source.owner
	}
}

// Selectors

private val predicateAliveAndNotSpectating = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.NOT_SPECTATING)
private val predicateAliveAndTargetable = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.CAN_AI_TARGET) // UPDATE: Make sure CAN_AI_TARGET still only checks creative/spectator mode
private val predicateAlwaysTrue = Predicates.alwaysTrue<Entity>()

/**
 * Selects all entities which are not spectators.
 */
val World.selectEntities
	get() = EntitySelector(this, EntitySelectors.NOT_SPECTATING)

/**
 * Selects all entities which have not been removed from the world, and are not spectators.
 */
val World.selectExistingEntities
	get() = EntitySelector(this, predicateAliveAndNotSpectating)

/**
 * Selects all entities which have not been removed from the world, and are not spectators or creative mode players.
 */
val World.selectVulnerableEntities
	get() = EntitySelector(this, predicateAliveAndTargetable)

/**
 * Selects all entities and spectators.
 */
val World.selectEntitiesAndSpectators
	get() = EntitySelector(this, predicateAlwaysTrue)
