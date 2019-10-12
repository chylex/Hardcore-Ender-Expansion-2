package chylex.hee.game.mechanics.damage
import chylex.hee.game.mechanics.damage.Damage.Companion.CANCEL_DAMAGE
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.clone
import chylex.hee.system.util.setFireTicks
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.potion.PotionEffect
import net.minecraft.util.CombatRules
import net.minecraft.world.EnumDifficulty.EASY
import net.minecraft.world.EnumDifficulty.HARD
import net.minecraft.world.EnumDifficulty.NORMAL
import net.minecraft.world.EnumDifficulty.PEACEFUL
import kotlin.math.nextUp

interface IDamageProcessor{
	@JvmDefault fun setup(properties: DamageProperties.Writer){}
	@JvmDefault fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader) = amount
	@JvmDefault fun afterDamage(target: Entity, properties: DamageProperties.Reader){}
	
	companion object{
		
		// Types
		
		@JvmField
		val PROJECTILE_TYPE = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.addType(DamageType.PROJECTILE)
			}
		}
		
		@JvmStatic
		fun FIRE_TYPE(setOnFireTicks: Int = 0) = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer) = properties.addType(DamageType.FIRE)
			
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				if (setOnFireTicks > 0){
					target.setFireTicks(setOnFireTicks)
				}
			}
		}
		
		@JvmField
		val BLAST_TYPE = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.addType(DamageType.BLAST)
			}
		}
		
		@JvmField
		val MAGIC_TYPE = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.addType(DamageType.MAGIC)
			}
		}
		
		// Difficulty
		
		@JvmField
		val PEACEFUL_EXCLUSION = object : IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					CANCEL_DAMAGE
			}
		}
		
		@JvmField
		val PEACEFUL_KNOCKBACK = object : IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					0F.nextUp()
			}
		}
		
		@JvmField
		val DIFFICULTY_SCALING = object : IDamageProcessor{
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
		
		@JvmStatic
		fun ARMOR_PROTECTION(allowShield: Boolean) = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				if (allowShield){
					properties.setAllowArmorAndShield()
				}
				else{
					properties.setAllowArmor()
				}
			}
		}
		
		@JvmField
		val ENCHANTMENT_PROTECTION = object : IDamageProcessor{
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
		
		@JvmField
		val NUDITY_DANGER = object : IDamageProcessor{
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
		
		@JvmField
		val POTION_PROTECTION = object : IDamageProcessor{
			/**
			 * [EntityLivingBase.applyPotionDamageCalculations]
			 */
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target !is EntityLivingBase)
					amount
				else
					amount * (1F - (0.15F * (target.getActivePotionEffect(Potions.RESISTANCE)?.amplifier?.plus(1)?.coerceAtMost(5) ?: 0)))
			}
		}
		
		@JvmStatic
		fun STATUS(effect: PotionEffect) = object : IDamageProcessor{
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				if (target is EntityLivingBase){
					target.addPotionEffect(effect.clone())
				}
			}
		}
		
		// Invincibility
		
		@JvmField
		val DEAL_CREATIVE = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.setDealCreative()
			}
		}
		
		@JvmStatic
		fun RAPID_DAMAGE(reduceByTicks: Int) = object : IDamageProcessor{
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				target.hurtResistantTime = (target.hurtResistantTime - reduceByTicks).coerceAtLeast(1)
			}
		}
		
		@JvmStatic
		fun IGNORE_INVINCIBILITY() = object : IDamageProcessor{
			private var prevHurtResistantTime = 0
			
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				prevHurtResistantTime = target.hurtResistantTime
				target.hurtResistantTime = 0
				return amount
			}
			
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				target.hurtResistantTime = prevHurtResistantTime
			}
		}
		
		@JvmField
		val NON_LETHAL = object : IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.setNonLethal()
			}
			
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target is EntityLivingBase)
					amount
				else
					CANCEL_DAMAGE
			}
		}
		
		// Helpers
		
		@JvmField
		val ALL_PROTECTIONS = arrayOf(ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION, POTION_PROTECTION)
		
		@JvmField
		val ALL_PROTECTIONS_WITH_SHIELD = arrayOf(ARMOR_PROTECTION(true), ENCHANTMENT_PROTECTION, POTION_PROTECTION)
	}
}
