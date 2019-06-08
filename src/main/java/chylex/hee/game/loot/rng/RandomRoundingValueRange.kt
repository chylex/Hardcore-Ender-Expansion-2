package chylex.hee.game.loot.rng
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextRounded
import net.minecraft.world.storage.loot.RandomValueRange
import java.util.Random
import kotlin.math.ceil
import kotlin.math.floor

class RandomRoundingValueRange(min: Float, max: Float) : RandomValueRange(floor(min), ceil(max)){
	constructor(original: RandomValueRange) : this(original.min, original.max)
	
	private val realMin = min
	private val realMax = max
	
	override fun generateInt(rand: Random): Int{
		return rand.nextRounded(rand.nextFloat(realMin, realMax))
	}
	
	override fun generateFloat(rand: Random): Float{
		return generateInt(rand).toFloat()
	}
}
