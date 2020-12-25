package chylex.hee.game.mechanics.damage

import chylex.hee.HEE
import chylex.hee.game.entity.posVec
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.event.entity.living.LivingDamageEvent

class DamageProperties {
	private var typeBits = 0
	private var ignoreArmorAndConsequentlyShield = true // due to how vanilla handles unblockable damage, isUnblockable controls both armor and shield
	private var ignoreShield = true
	private var nonLethal = false
	private var dealCreative = false
	
	private fun hasType(type: DamageType): Boolean {
		return typeBits and (1 shl type.ordinal) != 0
	}
	
	inner class Writer {
		fun setAllowArmor() {
			ignoreArmorAndConsequentlyShield = false
		}
		
		fun setAllowArmorAndShield() {
			ignoreArmorAndConsequentlyShield = false
			ignoreShield = false
		}
		
		fun setNonLethal() {
			nonLethal = true
		}
		
		fun setDealCreative() {
			dealCreative = true
		}
		
		fun addType(type: DamageType) {
			typeBits = typeBits or (1 shl type.ordinal)
		}
	}
	
	inner class Reader {
		val ignoreArmor
			get() = this@DamageProperties.ignoreArmorAndConsequentlyShield
		
		val ignoreShield
			get() = this@DamageProperties.ignoreShield
		
		val nonLethal
			get() = this@DamageProperties.nonLethal
		
		val dealCreative
			get() = this@DamageProperties.dealCreative
		
		fun hasType(type: DamageType): Boolean {
			return this@DamageProperties.hasType(type)
		}
		
		fun createDamageSourceForCalculations(): DamageSource {
			return createDamageSource("", null, null)
		}
	}
	
	// Damage source
	
	fun createDamageSource(damageTitle: String, directSource: Entity?, remoteSource: Entity?): DamageSource = CustomDamageSource(damageTitle, directSource, remoteSource)
	
	private inner class CustomDamageSource(damageTitle: String, val directSource: Entity?, val remoteSource: Entity?) : DamageSource(damageTitle) {
		val isNonLethal
			get() = nonLethal
		
		override fun isDamageAbsolute() = true // ignore potions and enchantments by default
		override fun isDifficultyScaled() = false // ignore difficulty by default
		
		override fun isProjectile() = hasType(DamageType.PROJECTILE)
		override fun isFireDamage() = hasType(DamageType.FIRE)
		override fun isExplosion() = hasType(DamageType.BLAST)
		override fun isMagicDamage() = hasType(DamageType.MAGIC)
		
		override fun isUnblockable() = ignoreArmorAndConsequentlyShield
		override fun canHarmInCreative() = dealCreative
		
		override fun getImmediateSource(): Entity? = directSource
		override fun getTrueSource(): Entity? = remoteSource
		
		override fun getDamageLocation(): Vec3d? = // UPDATE 1.15 (make sure this is still only used for shield checking)
			if (ignoreShield)
				null
			else
				directSource?.let(Entity::posVec)
		
		override fun getDeathMessage(victim: EntityLivingBase): ITextComponent {
			if (directSource == null) {
				return super.getDeathMessage(victim)
			}
			
			val realSource = remoteSource ?: directSource
			val heldItem = (realSource as? EntityLivingBase)?.getHeldItem(MAIN_HAND) ?: ItemStack.EMPTY
			
			val translationKeyGeneric = "death.attack.$damageType"
			val translationKeyItem = "$translationKeyGeneric.item"
			
			return if (!heldItem.isEmpty && heldItem.hasDisplayName())
				TranslationTextComponent(translationKeyItem, victim.displayName, realSource.displayName, heldItem.textComponent)
			else
				TranslationTextComponent(translationKeyGeneric, victim.displayName, realSource.displayName)
		}
	}
	
	// Non-lethal damage handling
	
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onLivingDamage(e: LivingDamageEvent) {
			val source = e.source as? CustomDamageSource ?: return
			
			if (source.isNonLethal) {
				e.amount = e.amount.coerceIn(0F, e.entityLiving.health - 1F)
			}
		}
	}
}
