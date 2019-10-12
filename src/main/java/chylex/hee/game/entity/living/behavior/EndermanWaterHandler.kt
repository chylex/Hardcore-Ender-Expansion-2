package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.system.util.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.tryApplyModifier
import chylex.hee.system.util.tryRemoveModifier
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.util.DamageSource
import net.minecraft.util.ITickable
import net.minecraftforge.common.util.INBTSerializable

class EndermanWaterHandler(private val enderman: EntityMobAbstractEnderman): ITickable, INBTSerializable<TagCompound>{
	private companion object{
		private val DEBUFF_WEAKNESS = AttributeModifier("Water weakness", -0.5, OPERATION_MUL_INCR_INDIVIDUAL)
		
		private const val WET_COUNTER_TAG = "WetCounter"
		private const val DEBUFF_TICKS_TAG = "DebuffTicks"
	}
	
	private var wetCounter = 0
	private var debuffTicks = 0
	
	override fun update(){
		val isWet = enderman.isWet
		
		if (isWet){
			++wetCounter
			
			if (wetCounter == 1){
				debuffTicks = enderman.rng.nextInt(20 * 6, 20 * 8)
				updateDebuff()
			}
			else if (wetCounter > 80){
				enderman.attackTarget = null
				enderman.attackEntityFrom(DamageSource.DROWN, 3F) // causes teleportation attempt
			}
		}
		else{
			if (wetCounter > 65){
				wetCounter = 65
			}
			
			if (debuffTicks > 0 && --debuffTicks == 0){
				wetCounter = 0
				updateDebuff()
			}
		}
	}
	
	private fun updateDebuff(){
		if (debuffTicks > 0){
			enderman.isShaking = true
			enderman.getAttribute(ATTACK_DAMAGE).tryApplyModifier(DEBUFF_WEAKNESS)
		}
		else{
			enderman.isShaking = false
			enderman.getAttribute(ATTACK_DAMAGE).tryRemoveModifier(DEBUFF_WEAKNESS)
		}
	}
	
	override fun serializeNBT() = TagCompound().apply {
		setShort(WET_COUNTER_TAG, wetCounter.toShort())
		setShort(DEBUFF_TICKS_TAG, debuffTicks.toShort())
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt){
		wetCounter = getShort(WET_COUNTER_TAG).toInt()
		debuffTicks = getShort(DEBUFF_TICKS_TAG).toInt()
		updateDebuff()
	}
}
