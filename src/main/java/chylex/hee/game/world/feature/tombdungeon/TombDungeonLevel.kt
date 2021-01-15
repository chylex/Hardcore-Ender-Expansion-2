package chylex.hee.game.world.feature.tombdungeon

import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount.HIGH
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount.MEDIUM
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_BASIC
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_BORDER
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_SPLIT
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BASIC
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BORDER
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_SPLIT
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_SPACIOUS
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_DEEP_LONG
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_NARROW
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_SPACIOUS
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_SINGLE_NARROW
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_SINGLE_SPACIOUS
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.TOMB_RANDOM
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonAbstractPiece
import chylex.hee.system.math.floorToInt
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import java.util.Random
import kotlin.math.max
import kotlin.math.min

enum class TombDungeonLevel(val isFancy: Boolean, private val corridorFactor: Int, val mainRooms: IntRange, val sideRooms: IntRange, val dustPerRoom: IntRange) {
	FIRST (isFancy = false, corridorFactor = 1, mainRooms = 0..0, sideRooms = 0..0, dustPerRoom =  8..15),
	SECOND(isFancy = false, corridorFactor = 5, mainRooms = 1..1, sideRooms = 1..1, dustPerRoom =  9..21),
	THIRD (isFancy = false, corridorFactor = 2, mainRooms = 0..0, sideRooms = 2..3, dustPerRoom = 10..32),
	FOURTH(isFancy = true,  corridorFactor = 4, mainRooms = 2..2, sideRooms = 1..1, dustPerRoom = 15..40),
	LAST  (isFancy = true,  corridorFactor = 6, mainRooms = 1..1, sideRooms = 0..1, dustPerRoom = 20..48);
	
	fun getMainCorridorLength(rand: Random): Int {
		return rand.nextInt(45 + (corridorFactor * 2), 57) + (2 * (6 - corridorFactor)) + ((corridorFactor - 1) * rand.nextFloat(12.5F, 14.2F)).floorToInt() + (if (this == LAST) 10 else 0)
	}
	
	fun nextMainCorridorSplitLength(rand: Random, remaining: Int): Int {
		val lower = 1 + (remaining / 2.5).floorToInt()
		
		val upper = if (this == LAST)
			min(remaining, (remaining / 5) + 3)
		else
			min(remaining, (remaining / 3) + 7)
		
		return rand.nextInt(min(lower, upper), max(lower, upper))
	}
	
	fun getSidePathCount(rand: Random): Int {
		return rand.nextInt(1, 2) + ordinal + (rand.nextFloat(2.6, 4.2) * min(ordinal, 3)).floorToInt()
	}
	
	fun getTombCount(rand: Random): Int {
		return if (this == LAST)
			rand.nextInt(10, 15)
		else
			rand.nextInt(5, 7) + (ordinal / 2) + (rand.nextInt(2, 3) * ordinal)
	}
	
	fun pickTombGeneratorAndSpacing(rand: Random): Pair<Int, (Boolean) -> TombDungeonAbstractPiece> {
		return when(this) {
			FIRST -> when(rand.nextInt(0, 5)) {
				0    -> 7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC)
				1    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BASIC)
				2    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC, PIECE_TOMB_RANDOM_MASS_7X_BASIC)
				3    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
				4    -> 7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC, PIECE_TOMB_RANDOM_MASS_5X_SPLIT)
				else -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BASIC, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
			}
			
			SECOND -> when(rand.nextInt(0, 6)) {
				in 0..1 -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_SPACIOUS)
				2       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
				3       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_SPLIT, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
				4       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
				else    ->  7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BORDER)
			}
			
			THIRD -> when(rand.nextInt(0, 7)) {
				0       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_SPACIOUS)
				in 1..2 -> 11 to PIECE_TOMB_RANDOM_MASS_SPACIOUS.generateItem(rand)
				3       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_NARROW)
				in 4..5 ->  9 to PIECE_TOMB_RANDOM_MULTI_NARROW.generateItem(rand)
				6       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER)
				else    ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
			}
			
			FOURTH -> when(rand.nextInt(0, 9)) {
				in 0..2 ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_NARROW)
				3       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT)
				4       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_DEEP_LONG)
				5       -> 11 to PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT.generateItem(rand)
				6       -> 11 to PIECE_TOMB_RANDOM_MULTI_DEEP_LONG.generateItem(rand)
				in 7..8 -> 11 to PIECE_TOMB_RANDOM_MULTI_SPACIOUS.generateItem(rand)
				else    ->  5 to PIECE_TOMB_SINGLE_NARROW
			}
			
			else -> when(rand.nextInt(0, 7)) {
				in 0..2 ->  7 to PIECE_TOMB_SINGLE_SPACIOUS
				in 3..6 ->  5 to PIECE_TOMB_SINGLE_NARROW
				else    -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_SPACIOUS)
			}
		}
	}
	
	fun pickUndreadAndSpiderlingSpawns(rand: Random, amount: MobAmount): Pair<Int, Int> {
		val m = if (amount >= MEDIUM) 1 else 0
		val h = if (amount >= HIGH) 1 else 0
		
		return when(this) {
			FIRST -> when(rand.nextInt(0, 3)) {
				0    -> MOBS(undreads = rand.nextInt(0, h), spiderlings = 1 + m)
				1    -> MOBS(undreads = rand.nextInt(0, h), spiderlings = 2 + m)
				2    -> MOBS(undreads = 1 + h,              spiderlings = 0 + rand.nextInt(0, m))
				else -> MOBS(undreads = 1 + h,              spiderlings = 1 + rand.nextInt(0, m))
			}
			
			SECOND -> when(rand.nextInt(0, 3)) {
				0    -> MOBS(undreads = rand.nextInt(0, m + h),         spiderlings = 1 + rand.nextInt(0, m + h))
				1    -> MOBS(undreads = 1 + rand.nextInt(0, m + h),     spiderlings = rand.nextInt(0, m + h))
				2    -> MOBS(undreads = 1 + m + rand.nextInt(h, h * 2), spiderlings = 0)
				else -> MOBS(undreads = 1 + h,                          spiderlings = 1 + m + rand.nextInt(h, h * 3))
			}
			
			// TODO
			
			else -> 0 to 0
		}
		
		/*
		
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.system.math.floorToInt
import com.google.common.collect.Comparators
import java.util.Random

val rand = Random()
val level = TombDungeonLevel.FIRST
val reps = 100000

MobAmount.values().map { amount ->
	"\n\n" + amount.name + "\n" + "-".repeat(amount.name.length) + "\n" + (1..reps)
		.map { level.pickUndreadAndSpiderlingSpawns(rand, amount) }
		.groupingBy { it }
		.eachCount()
		.entries
		.map { ((it.value * 100.0) / reps).floorToInt() to it.key }
		.sortedWith(compareBy({ it.first }, { it.second.first }, { it.second.second }))
		.map { "U = ${it.second.first}, S = ${it.second.second} ... ${it.first} %" }
		.joinToString("\n")
}.joinToString("\n")
	
		 */
	}
	
	private fun MOBS(undreads: Int, spiderlings: Int) = undreads to spiderlings
	
	enum class MobAmount {
		LOW, MEDIUM, HIGH
	}
}
