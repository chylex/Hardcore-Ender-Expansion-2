package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.util.OP_MUL_INCR_INDIVIDUAL
import chylex.hee.game.entity.util.getAttributeInstance
import chylex.hee.game.entity.util.tryApplyNonPersistentModifier
import chylex.hee.game.entity.util.tryRemoveModifier
import chylex.hee.system.random.nextInt
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.util.DamageSource
import net.minecraftforge.common.util.INBTSerializable

class EndermanWaterHandler(private val enderman: EntityMobAbstractEnderman, private val takeDamageAfterWetTicks: Int) : INBTSerializable<TagCompound> {
	private companion object {
		private val DEBUFF_WEAKNESS = AttributeModifier("Water weakness", -0.5, OP_MUL_INCR_INDIVIDUAL)
		
		private const val WET_COUNTER_TAG = "WetCounter"
		private const val DEBUFF_TICKS_TAG = "DebuffTicks"
	}
	
	private var wetCounter = 0
	private var debuffTicks = 0
	
	fun update() {
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
			enderman.getAttributeInstance(ATTACK_DAMAGE).tryApplyNonPersistentModifier(DEBUFF_WEAKNESS)
		}
		else {
			enderman.isShaking = false
			enderman.getAttributeInstance(ATTACK_DAMAGE).tryRemoveModifier(DEBUFF_WEAKNESS)
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
