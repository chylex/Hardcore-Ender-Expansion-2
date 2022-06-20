package chylex.hee.game.entity.living

import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.entity.util.getAttributeInstance
import chylex.hee.game.entity.util.tryRemoveModifier
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.Pose
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.EndermanEntity
import net.minecraft.entity.projectile.AbstractArrowEntity
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.IndirectEntityDamageSource
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

abstract class EntityMobAbstractEnderman(type: EntityType<out EntityMobAbstractEnderman>, world: World) : EndermanEntity(type, world) {
	abstract class BaseType<T : EntityMobAbstractEnderman> : IHeeMobEntityType<T> {
		override val classification
			get() = EntityClassification.MONSTER
		
		override val size
			get() = EntitySize(0.6F, 2.9F)
	}
	
	private companion object {
		private val DATA_SHAKING = EntityData.register<EntityMobAbstractEnderman, Boolean>(DataSerializers.BOOLEAN)
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD, NUDITY_DANGER)
	}
	
	var isAggro: Boolean by EntityData(SCREAMING)
	var isShaking: Boolean by EntityData(DATA_SHAKING)
	
	abstract val teleportCooldown: Int
	
	override fun registerData() {
		super.registerData()
		dataManager.register(DATA_SHAKING, false)
	}
	
	override fun registerGoals() {}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun updateAITasks() {} // blocks vanilla water damage & sunlight behavior
	
	override fun setAttackTarget(newTarget: LivingEntity?) {
		if (attackTarget === newTarget) {
			return
		}
		
		val prevAggressive = isAggro
		super.setAttackTarget(newTarget)
		isAggro = prevAggressive
		
		if (newTarget != null) {
			getAttributeInstance(MOVEMENT_SPEED).tryRemoveModifier(ATTACKING_SPEED_BOOST) // vanilla adds speed boost attribute in this case
		}
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		if (source is IndirectEntityDamageSource) {
			val result = super.attackEntityFrom(FakeIndirectDamageSource(source), amount)
			
			if (result) {
				(source.immediateSource as? AbstractArrowEntity)?.remove() // of course vanilla hardcodes not destroying arrows with Endermen...
			}
			
			return result
		}
		
		return super.attackEntityFrom(source, amount)
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	open fun canTeleportTo(aabb: AxisAlignedBB): Boolean {
		return world.hasNoCollisions(this, aabb) && !world.containsAnyLiquid(aabb)
	}
	
	override fun teleportRandomly(): Boolean {
		return false
	}
	
	override fun isScreaming(): Boolean {
		return false // disables vanilla fx
	}
	
	override fun getCreatureAttribute(): CreatureAttribute {
		return CustomCreatureType.ENDER
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: net.minecraft.entity.EntitySize): Float {
		return 2.62F
	}
	
	private class FakeIndirectDamageSource(private val source: IndirectEntityDamageSource) : EntityDamageSource(source.damageType, source.immediateSource) {
		override fun getImmediateSource() = source.immediateSource
		override fun getTrueSource() = source.trueSource
		
		override fun isProjectile() = source.isProjectile
		override fun isFireDamage() = source.isFireDamage
		override fun isExplosion() = source.isExplosion
		override fun isMagicDamage() = source.isMagicDamage
		override fun getIsThornsDamage() = source.isThornsDamage
		
		override fun isUnblockable() = source.isUnblockable
		override fun isDamageAbsolute() = source.isDamageAbsolute
		override fun canHarmInCreative() = source.canHarmInCreative()
		
		override fun getDeathMessage(victim: LivingEntity): ITextComponent = source.getDeathMessage(victim)
	}
}
