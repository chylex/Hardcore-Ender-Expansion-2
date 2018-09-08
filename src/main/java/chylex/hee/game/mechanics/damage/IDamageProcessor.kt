package chylex.hee.game.mechanics.damage
import chylex.hee.game.mechanics.damage.DamageProperties.Reader
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects.RESISTANCE
import net.minecraft.item.ItemArmor
import net.minecraft.potion.PotionEffect
import net.minecraft.util.CombatRules
import net.minecraft.world.EnumDifficulty.EASY
import net.minecraft.world.EnumDifficulty.HARD
import net.minecraft.world.EnumDifficulty.NORMAL
import net.minecraft.world.EnumDifficulty.PEACEFUL
import kotlin.math.nextUp

interface IDamageProcessor{
	fun setup(properties: DamageProperties.Writer){}
	fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float = amount
	fun afterDamage(target: Entity, properties: DamageProperties.Reader){}
	
	companion object{ // TODO make static fields in kotlin 1.3 and use default methods
		const val CANCEL_DAMAGE = 0F
		
		// Types
		
		val PROJECTILE_TYPE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer) = properties.addType(DamageType.PROJECTILE)
		}
		
		fun FIRE_TYPE(setOnFireSeconds: Int = 0) = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer) = properties.addType(DamageType.FIRE)
			
			override fun afterDamage(target: Entity, properties: Reader){
				if (setOnFireSeconds > 0){
					target.setFire(setOnFireSeconds)
				}
			}
		}
		
		val BLAST_TYPE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer) = properties.addType(DamageType.BLAST)
		}
		
		val MAGIC_TYPE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer) = properties.addType(DamageType.MAGIC)
		}
		
		// Difficulty
		
		val PEACEFUL_EXCLUSION = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					CANCEL_DAMAGE
			}
		}
		
		val PEACEFUL_KNOCKBACK = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					0F.nextUp()
			}
		}
		
		val DIFFICULTY_SCALING = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target !is EntityPlayer){
					amount
				}
				else when(target.world.difficulty!!){
					PEACEFUL -> amount * 0.4F
					EASY     -> amount * 0.7F
					NORMAL   -> amount
					HARD     -> amount * 1.4F
				}
			}
		}
		
		// Equipment
		
		fun ARMOR_PROTECTION(allowShield: Boolean) = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				if (allowShield){
					properties.setAllowArmorAndShield()
				}
				else{
					properties.setAllowArmor()
				}
			}
		}
		
		val ENCHANTMENT_PROTECTION = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				if (target !is EntityLivingBase){
					return amount
				}
				
				val enchantmentProtection = EnchantmentHelper.getEnchantmentModifierDamage(target.armorInventoryList, properties.createDamageSourceForCalculations())
				
				return if (enchantmentProtection <= 0)
					amount
				else
					CombatRules.getDamageAfterMagicAbsorb(amount, enchantmentProtection.toFloat())
			}
		}
		
		val NUDITY_DANGER = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				val bodyCoverageFactor = target.armorInventoryList.sumBy {
					if (it.item is ItemArmor)
						2
					else if (!it.isEmpty)
						1
					else
						0
				}
				
				return amount * when(bodyCoverageFactor){
					0 -> 2.5F
					1 -> 2.3F
					2 -> 2.1F
					3 -> 1.9F
					4 -> 1.7F
					5 -> 1.5F
					6 -> 1.3F
					7 -> 1.15F
					else -> 1F
				}
			}
		}
		
		// Status effects
		
		val POTION_PROTECTION = object: IDamageProcessor{
			/**
			 * [EntityLivingBase.applyPotionDamageCalculations]
			 */
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target !is EntityLivingBase)
					amount
				else
					amount * (1F - (0.15F * (target.getActivePotionEffect(RESISTANCE)?.amplifier?.plus(1)?.coerceAtMost(5) ?: 0)))
			}
		}
		
		fun STATUS(effect: PotionEffect) = object: IDamageProcessor{
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				if (target is EntityLivingBase){
					target.addPotionEffect(effect)
				}
			}
		}
		
		// Invincibility
		
		val DEAL_CREATIVE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.setDealCreative()
			}
		}
		
		fun RAPID_DAMAGE(reduceByTicks: Int) = object: IDamageProcessor{
			override fun afterDamage(target: Entity, properties: Reader){
				target.hurtResistantTime = (target.hurtResistantTime - reduceByTicks).coerceAtLeast(1)
			}
		}
		
		fun IGNORE_INVINCIBILITY() = object: IDamageProcessor{
			private var prevHurtResistantTime = 0
			
			override fun modifyDamage(amount: Float, target: Entity, properties: Reader): Float{
				prevHurtResistantTime = target.hurtResistantTime
				target.hurtResistantTime = 0
				return amount
			}
			
			override fun afterDamage(target: Entity, properties: Reader){
				target.hurtResistantTime = prevHurtResistantTime
			}
		}
	}
}
