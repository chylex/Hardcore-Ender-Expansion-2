package chylex.hee.game.mechanics.damage
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.translation.I18n

class DamageProperties{
	private var typeBits = 0
	private var ignoreArmorAndConsequentlyShield = true // due to how vanilla handles unblockable damage, isUnblockable controls both armor and shield
	private var ignoreShield = true
	private var dealCreative = false
	
	private inline fun hasType(type: DamageType): Boolean{
		return typeBits and (1 shl type.ordinal) != 0
	}
	
	inner class Writer{
		fun setAllowArmor(){
			ignoreArmorAndConsequentlyShield = false
		}
		
		fun setAllowArmorAndShield(){
			ignoreArmorAndConsequentlyShield = false
			ignoreShield = false
		}
		
		fun setDealCreative(){
			dealCreative = true
		}
		
		fun addType(type: DamageType){
			typeBits = typeBits or (1 shl type.ordinal)
		}
	}
	
	inner class Reader{
		val ignoreArmor
			get() = this@DamageProperties.ignoreArmorAndConsequentlyShield
		
		val ignoreShield
			get() = this@DamageProperties.ignoreShield
		
		val dealCreative
			get() = this@DamageProperties.dealCreative
		
		fun hasType(type: DamageType): Boolean{
			return this@DamageProperties.hasType(type)
		}
		
		fun createDamageSourceForCalculations(): DamageSource{
			return createDamageSource("", null, null)
		}
	}
	
	// Damage source
	
	fun createDamageSource(damageTitle: String, triggeringSource: Entity?, remoteSource: Entity?): DamageSource = CustomDamageSource(damageTitle, triggeringSource, remoteSource)
	
	private inner class CustomDamageSource(damageTitle: String, val triggeringSource: Entity?, val remoteSource: Entity?) : DamageSource(damageTitle){
		override fun isDamageAbsolute(): Boolean = true // ignore potions and enchantments by default
		override fun isDifficultyScaled(): Boolean = false // ignore difficulty by default
		
		override fun isProjectile(): Boolean = hasType(DamageType.PROJECTILE)
		override fun isFireDamage(): Boolean = hasType(DamageType.FIRE)
		override fun isExplosion(): Boolean = hasType(DamageType.BLAST)
		override fun isMagicDamage(): Boolean = hasType(DamageType.MAGIC)
		
		override fun isUnblockable(): Boolean = ignoreArmorAndConsequentlyShield
		override fun canHarmInCreative(): Boolean = dealCreative
		
		override fun getImmediateSource(): Entity? = triggeringSource
		override fun getTrueSource(): Entity? = remoteSource
		
		override fun getDamageLocation(): Vec3d? = // UPDATE: Make sure this is still only used for shield checking
			if (ignoreShield)
				null
			else
				triggeringSource?.let { Vec3d(it.posX, it.posY, it.posZ) }
		
		override fun getDeathMessage(victim: EntityLivingBase): ITextComponent{
			if (triggeringSource == null){
				return super.getDeathMessage(victim)
			}
			
			val realSource = remoteSource ?: triggeringSource
			val heldItem = (realSource as? EntityLivingBase)?.getHeldItem(MAIN_HAND) ?: ItemStack.EMPTY
			
			val translationKeyGeneric = "death.attack.$damageType"
			val translationKeyItem = "$translationKeyGeneric.item"
			
			return if (!heldItem.isEmpty && heldItem.hasDisplayName() && I18n.canTranslate(translationKeyItem))
				TextComponentTranslation(translationKeyItem, victim.displayName, realSource.displayName, heldItem.textComponent)
			else
				TextComponentTranslation(translationKeyGeneric, victim.displayName, realSource.displayName)
		}
	}
}