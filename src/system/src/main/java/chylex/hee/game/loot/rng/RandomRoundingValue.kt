package chylex.hee.game.loot.rng

import chylex.hee.system.facades.Resource
import chylex.hee.system.random.nextRounded
import net.minecraft.loot.IRandomRange
import net.minecraft.util.ResourceLocation
import java.util.Random

class RandomRoundingValue(private val value: Float) : IRandomRange {
	companion object {
		val LOCATION = Resource.Custom("rounding")
	}
	
	override fun generateInt(rand: Random): Int {
		return rand.nextRounded(value)
	}
	
	override fun getType(): ResourceLocation {
		return LOCATION
	}
}
