package chylex.hee.test.main

import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import java.util.Random
import kotlin.math.roundToInt

fun main() {
	val rand = Random()
	
	for(level in TombDungeonLevel.values()) {
		val reps = 100000
		
		println("\n")
		println("-".repeat(level.name.length))
		println(level.name)
		println("-".repeat(level.name.length))
		println(MobAmount.values().joinToString("\n") { amount ->
			"\n " + amount.name + "\n\n" + (1..reps)
				.map { level.pickUndreadAndSpiderlingSpawns(rand, amount) }
				.groupingBy { it }
				.eachCount()
				.entries
				.map { ((it.value * 100.0) / reps).roundToInt() to it.key }
				.sortedWith(compareBy({ -it.first }, { -it.second.first }, { -it.second.second }))
				.joinToString("\n") { "  U = ${it.second.first}, S = ${it.second.second} ... ${it.first} %" }
		})
	}
}
