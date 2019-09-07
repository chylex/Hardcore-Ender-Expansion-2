package chylex.hee.game.entity.living
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.util.EntityData
import chylex.hee.system.util.tryRemoveModifier
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.EntityDamageSourceIndirect
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

abstract class EntityMobAbstractEnderman(world: World) : EntityEnderman(world){
	private companion object{
		private val DATA_SHAKING = EntityData.register<EntityMobAbstractEnderman, Boolean>(DataSerializers.BOOLEAN)
	}
	
	var isAggressive: Boolean by EntityData(SCREAMING)
	var isShaking: Boolean by EntityData(DATA_SHAKING)
	
	abstract val teleportCooldown: Int
	
	override fun entityInit(){
		super.entityInit()
		dataManager.register(DATA_SHAKING, false)
	}
	
	override fun initEntityAI(){}
	
	override fun updateAITasks(){} // blocks vanilla water damage & sunlight behavior
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (attackTarget === newTarget){
			return
		}
		
		val prevAggressive = isAggressive
		super.setAttackTarget(newTarget)
		isAggressive = prevAggressive
		
		if (newTarget != null){
			getEntityAttribute(MOVEMENT_SPEED).tryRemoveModifier(ATTACKING_SPEED_BOOST)
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
	
	override fun getCreatureAttribute(): EnumCreatureAttribute{
		return CustomCreatureType.ENDER
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
