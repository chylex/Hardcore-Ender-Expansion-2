package chylex.hee.test.main

import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import java.util.Locale.ROOT
import java.util.Random
import kotlin.math.roundToInt

fun main() {
	val rand = Random()
	
	for(level in TombDungeonLevel.values()) {
		val reps = 100000
		
		println()
		println("== ${level.name} ${"=".repeat(24 - level.name.length)}")
		
		for(amount in MobAmount.values()) {
			println()
			println("   ${amount.name}")
			println()
			
			val results = (1..reps)
				.map { level.pickUndreadAndSpiderlingSpawns(rand, amount) }
				.groupingBy { it }
				.eachCount()
				.entries
				.map { ((it.value * 100.0) / reps).roundToInt() to it.key }
				.sortedWith(compareBy({ -it.first }, { -it.second.first }, { -it.second.second }))
			
			println(results.joinToString("\n") { "    U = ${it.second.first}     S = ${it.second.second}     ${it.first.toString().padStart(2)} %" })
			println("    U ~ ${"%.1f".format(ROOT, results.sumOf { it.second.first * it.first } * 0.01)}   S ~ ${"%.1f".format(ROOT, results.sumOf { it.second.second * it.first } * 0.01)}")
		}
	}
}
