package chylex.hee.game.potion
import chylex.hee.HEE
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.Potion
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
	fun shouldCorrupt(potion: Potion, entity: EntityLivingBase): Boolean{
		return potion.isBeneficial && entity.isPotionActive(PotionCorruption) // PotionCorruption being set as harmful also prevents infinite loop from isPotionActive
	}
	
	@JvmStatic
	fun shouldCorrupt(effect: EffectInstance, entity: EntityLivingBase): Boolean{
		return effect.potion.isBeneficial && entity.isPotionActive(PotionCorruption)
	}
	
	@JvmStatic
	fun shouldSkipAttributeChange(potion: Potion, entity: EntityLivingBase): Boolean{
		return !pauseAttributeSkipping.get() && canCorruptAttributes(potion) && entity.isPotionActive(PotionCorruption)
	}
	
	private fun canCorruptAttributes(potion: Potion): Boolean{
		// TODO skip corrupting attributes on overriding classes, currently it doesn't work with Absorption and could mess with modded ones that do similar things
		return potion.isBeneficial && potion.javaClass.let { it === Effect::class.java || it === AttackDamageEffect::class.java || it === HealthBoostEffect::class.java }
	}
	
	private inline fun processCurrentlyRunningPotions(entity: EntityLivingBase, callback: (EffectInstance) -> Unit){
		pauseAttributeSkipping.set(true)
		
		for((potion, effect) in entity.activePotionMap){
			if (canCorruptAttributes(potion)){
				callback(effect)
			}
		}
		
		pauseAttributeSkipping.set(false)
	}
	
	override fun applyAttributesModifiersToEntity(entity: EntityLivingBase, attributes: AbstractAttributeMap, amplifier: Int){
		processCurrentlyRunningPotions(entity){ it.potion.removeAttributesModifiersFromEntity(entity, attributes, it.amplifier) }
	}
	
	override fun removeAttributesModifiersFromEntity(entity: EntityLivingBase, attributes: AbstractAttributeMap, amplifier: Int){
		processCurrentlyRunningPotions(entity){ it.potion.applyAttributesModifiersToEntity(entity, attributes, it.amplifier) }
	}
}
