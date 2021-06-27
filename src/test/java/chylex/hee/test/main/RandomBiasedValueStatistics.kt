package chylex.hee.test.main
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import java.util.Random
import kotlin.math.roundToInt

fun main() {
	val min = 3F
	val max = 10F
	val highestChanceValue = 6.5F
	val biasSoftener = 3F
	
	val rand = Random()
	val generator = RandomBiasedValueRange(min, max, highestChanceValue, biasSoftener)
	val reps = 100000
	
	val results = (1..reps).map { generator.generateInt(rand) }
		.groupBy { it }
		.mapValues { it.value.size }
		.toList()
		.sortedBy { it.first }
	
	val pad1 = results.maxOf { it.first.toString().length }
	val pad2 = results.maxOf { it.second.toString().length }
	
	println(results.joinToString("\n") {
		"${it.first.toString().padStart(pad1)} | ${it.second.toString().padStart(pad2)} | ${((it.second * 100.0) / reps).roundToInt().toString().padStart(2)} %"
	})
}
