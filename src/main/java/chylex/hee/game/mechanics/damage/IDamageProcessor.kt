package chylex.hee.game.mechanics.damage
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
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
		
		// Difficulty or game mode
		
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
		
		val DEAL_CREATIVE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.setDealCreative()
			}
		}
	}
}
