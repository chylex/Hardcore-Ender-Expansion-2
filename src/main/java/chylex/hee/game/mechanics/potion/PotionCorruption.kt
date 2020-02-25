package chylex.hee.game.mechanics.potion
import chylex.hee.HEE
import chylex.hee.game.mechanics.potion.brewing.PotionTypeMap
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.util.color.IntColor.Companion.RGB
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.AbstractAttributeMap
import net.minecraft.potion.AttackDamageEffect
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraft.potion.HealthBoostEffect

@SubscribeAllEvents(modid = HEE.ID)
object PotionCorruption : Potion(HARMFUL, RGB(1, 1, 1).i){
	val TYPE
		get() = PotionTypeMap.getType(this)
	
	private val pauseAttributeSkipping = ThreadLocal.withInitial { false }
	
	@JvmStatic
	@Suppress("unused")
	fun shouldCorrupt(potion: Potion, entity: EntityLivingBase): Boolean{
		return potion.isBeneficial && entity.isPotionActive(PotionCorruption) // PotionCorruption being set as harmful also prevents infinite loop from isPotionActive
	}
	
	@JvmStatic
	@Suppress("unused")
	fun shouldCorrupt(effect: EffectInstance, entity: EntityLivingBase): Boolean{
		return effect.potion.isBeneficial && entity.isPotionActive(PotionCorruption)
	}
	
	@JvmStatic
	@Suppress("unused")
	fun shouldSkipAttributeChange(potion: Potion, entity: EntityLivingBase): Boolean{
		return !pauseAttributeSkipping.get() && canCorruptAttributes(potion) && entity.isPotionActive(PotionCorruption)
	}
	
	private fun canCorruptAttributes(potion: Potion): Boolean{
		// TODO skip corrupting attributes on overriding classes, currently it doesn't work with Absorption and could mess with modded ones that do similar things
		return potion.isBeneficial && potion.javaClass.let { it === Effect::class.java || it === AttackDamageEffect::class.java || it === HealthBoostEffect::class.java }
	}
	
	private inline fun processCurrentlyRunningPotions(entity: LivingEntity, callback: (EffectInstance) -> Unit){
		pauseAttributeSkipping.set(true)
		
		for((potion, effect) in entity.activePotionMap){
			if (canCorruptAttributes(potion)){
				callback(effect)
			}
		}
		
		pauseAttributeSkipping.set(false)
	}
	
	override fun applyAttributesModifiersToEntity(entity: LivingEntity, attributes: AbstractAttributeMap, amplifier: Int){
		processCurrentlyRunningPotions(entity){ it.potion.removeAttributesModifiersFromEntity(entity, attributes, it.amplifier) }
	}
	
	override fun removeAttributesModifiersFromEntity(entity: LivingEntity, attributes: AbstractAttributeMap, amplifier: Int){
		processCurrentlyRunningPotions(entity){ it.potion.applyAttributesModifiersToEntity(entity, attributes, it.amplifier) }
	}
}
