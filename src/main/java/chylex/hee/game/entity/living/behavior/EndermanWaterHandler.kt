package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.tryApplyModifier
import chylex.hee.game.entity.tryRemoveModifier
import chylex.hee.system.component.general.SerializableComponent
import chylex.hee.system.component.general.TickableComponent
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.util.DamageSource

class EndermanWaterHandler(private val enderman: EntityMobAbstractEnderman, private val takeDamageAfterWetTicks: Int) : TickableComponent, SerializableComponent {
	private companion object {
		private val DEBUFF_WEAKNESS = AttributeModifier("Water weakness", -0.5, OPERATION_MUL_INCR_INDIVIDUAL)
		
		private const val WET_COUNTER_TAG = "WetCounter"
		private const val DEBUFF_TICKS_TAG = "DebuffTicks"
	}
	
	override val serializationKey
		get() = "Water"
	
	private var wetCounter = 0
	private var debuffTicks = 0
	
	override fun tickServer() {
		val isWet = enderman.isWet
		
		if (isWet) {
			++wetCounter
			
			if (wetCounter == 1) {
				debuffTicks = enderman.rng.nextInt(20 * 6, 20 * 8)
				updateDebuff()
			}
			else if (wetCounter > takeDamageAfterWetTicks) {
				enderman.attackTarget = null
				enderman.attackEntityFrom(DamageSource.DROWN, 3F) // causes teleportation attempt
			}
		}
		else {
			if (wetCounter > 65) {
				wetCounter = 65
			}
			
			if (debuffTicks > 0 && --debuffTicks == 0) {
				wetCounter = 0
				updateDebuff()
			}
		}
	}
	
	private fun updateDebuff() {
		if (debuffTicks > 0) {
			enderman.isShaking = true
			enderman.getAttribute(ATTACK_DAMAGE).tryApplyModifier(DEBUFF_WEAKNESS)
		}
		else {
			enderman.isShaking = false
			enderman.getAttribute(ATTACK_DAMAGE).tryRemoveModifier(DEBUFF_WEAKNESS)
		}
	}
	
	override fun serializeNBT() = TagCompound().apply {
		putShort(WET_COUNTER_TAG, wetCounter.toShort())
		putShort(DEBUFF_TICKS_TAG, debuffTicks.toShort())
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		wetCounter = getShort(WET_COUNTER_TAG).toInt()
		debuffTicks = getShort(DEBUFF_TICKS_TAG).toInt()
		updateDebuff()
	}
}
