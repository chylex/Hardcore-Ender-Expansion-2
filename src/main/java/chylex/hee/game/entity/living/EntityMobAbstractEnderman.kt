package chylex.hee.game.entity.living
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.EntityDamageSourceIndirect
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

abstract class EntityMobAbstractEnderman(world: World) : EntityEnderman(world){
	var isAggressive
		get() = super.isScreaming()
		set(value) = dataManager.set(SCREAMING, value)
	
	override fun initEntityAI(){}
	
	override fun updateAITasks(){}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		val prevAggressive = isAggressive
		super.setAttackTarget(newTarget)
		isAggressive = prevAggressive
		
		if (newTarget != null){
			getEntityAttribute(MOVEMENT_SPEED).removeModifier(ATTACKING_SPEED_BOOST)
		}
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (source is EntityDamageSourceIndirect){
			val result = super.attackEntityFrom(FakeIndirectDamageSource(source), amount)
			
			if (result){
				(source.immediateSource as? EntityArrow)?.setDead() // of course vanilla hardcodes not destroying arrows with Endermen...
			}
			
			return result
		}
		
		return super.attackEntityFrom(source, amount)
	}
	
	override fun teleportRandomly(): Boolean{
		return false
	}
	
	override fun teleportToEntity(entity: Entity): Boolean{
		return false
	}
	
	override fun isScreaming(): Boolean{
		return false // disables vanilla fx
	}
	
	override fun getLootTable(): ResourceLocation?{
		return null
	}
	
	private class FakeIndirectDamageSource(private val source: EntityDamageSourceIndirect) : EntityDamageSource(source.damageType, source.immediateSource){
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
		
		override fun getDeathMessage(victim: EntityLivingBase): ITextComponent? = source.getDeathMessage(victim)
	}
}
