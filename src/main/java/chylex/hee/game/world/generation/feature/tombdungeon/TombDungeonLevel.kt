package chylex.hee.game.world.generation.feature.tombdungeon

import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount.HIGH
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount.MEDIUM
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_BASIC
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_BORDER
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_5X_SPLIT
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BASIC
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BORDER
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_7X_SPLIT
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MASS_SPACIOUS
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_DEEP_LONG
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_NARROW
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_RANDOM_MULTI_SPACIOUS
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_SINGLE_NARROW
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.PIECE_TOMB_SINGLE_SPACIOUS
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.TOMB_RANDOM
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonAbstractPiece
import chylex.hee.util.math.floorToInt
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextRounded
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
		return rand.nextInt(45 + (corridorFactor * 2), 57) + (2 * (6 - corridorFactor)) + ((corridorFactor - 1) * rand.nextFloat(12.5F, 14.2F)).floorToInt() + (if (this == LAST) 13 else 0)
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
			rand.nextInt(18, 21)
		else
			rand.nextInt(5, 7) + ((ordinal + 1) / 2) + (rand.nextInt(2, 3) * ordinal)
	}
	
	fun pickTombGeneratorAndSpacing(rand: Random): Pair<Int, (Boolean) -> TombDungeonAbstractPiece> {
		return when (this) {
			FIRST -> when (rand.nextInt(0, 5)) {
				0    -> 7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC)
				1    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BASIC)
				2    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC, PIECE_TOMB_RANDOM_MASS_7X_BASIC)
				3    -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
				4    -> 7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BASIC, PIECE_TOMB_RANDOM_MASS_5X_SPLIT)
				else -> 9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BASIC, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
			}
			
			SECOND -> when (rand.nextInt(0, 6)) {
				in 0..1 -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_SPACIOUS)
				2       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
				3       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_SPLIT, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
				4       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER, PIECE_TOMB_RANDOM_MASS_7X_SPLIT)
				else    ->  7 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_5X_BORDER)
			}
			
			THIRD -> when (rand.nextInt(0, 8)) {
				0       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_SPACIOUS)
				in 1..2 -> 11 to PIECE_TOMB_RANDOM_MASS_SPACIOUS.generateItem(rand)
				3       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_NARROW)
				in 4..5 ->  9 to PIECE_TOMB_RANDOM_MULTI_NARROW.generateItem(rand)
				6       ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER)
				else    ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT)
			}
			
			FOURTH -> when (rand.nextInt(0, 9)) {
				in 0..2 ->  9 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_NARROW)
				3       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT)
				4       -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_DEEP_LONG)
				5       -> 11 to PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT.generateItem(rand)
				6       -> 11 to PIECE_TOMB_RANDOM_MULTI_DEEP_LONG.generateItem(rand)
				in 7..8 -> 11 to PIECE_TOMB_RANDOM_MULTI_SPACIOUS.generateItem(rand)
				else    ->  5 to PIECE_TOMB_SINGLE_NARROW
			}
			
			else -> when (rand.nextInt(0, 11)) {
				in 0..3  ->  7 to PIECE_TOMB_SINGLE_SPACIOUS
				in 4..8  ->  5 to PIECE_TOMB_SINGLE_NARROW
				in 9..10 -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_SPACIOUS)
				else     -> 11 to TOMB_RANDOM(rand, PIECE_TOMB_RANDOM_MULTI_DEEP_LONG)
			}
		}
	}
	
	fun pickUndreadAndSpiderlingSpawns(rand: Random, amount: MobAmount): Pair<Int, Int> {
		val m = if (amount >= MEDIUM) 1 else 0
		val h = if (amount >= HIGH) 1 else 0
		
		return when (this) {
			FIRST -> when (rand.nextInt(0, 3)) {
				0    -> MOBS(undreads = rand.nextRounded(0.2F),     spiderlings = 1 + m + rand.nextInt(0, h))
				1    -> MOBS(undreads = rand.nextRounded(0.3F * m), spiderlings = 1 + h + rand.nextInt(0, m))
				2    -> MOBS(undreads = rand.nextRounded(0.3F * h), spiderlings = 2 + m)
				else -> MOBS(undreads = 0,                          spiderlings = 1 + rand.nextInt(0, 1 + m))
			}
			
			SECOND -> when (rand.nextInt(0, 3)) {
				0    -> MOBS(undreads = rand.nextRounded(0.3F),     spiderlings = 1 + m + rand.nextInt(0, h))
				1    -> MOBS(undreads = rand.nextRounded(0.4F * m), spiderlings = 1 + rand.nextInt(0, 2 * m))
				else -> MOBS(undreads = 0,                          spiderlings = rand.nextRounded(1.8F + (m * 0.55F) + (h * 0.55F)))
			}
			
			THIRD -> when (rand.nextInt(0, 2)) {
				0    -> MOBS(undreads = rand.nextInt(0, 1 + h), spiderlings = rand.nextRounded(1.4F + (m * 0.5F)) + rand.nextInt(0, m))
				1    -> MOBS(undreads = rand.nextInt(0, 1 + m), spiderlings = rand.nextRounded(1.2F) + rand.nextInt(0, 2 * h))
				else -> MOBS(undreads = rand.nextInt(0, 1 + m), spiderlings = 2 + rand.nextInt(0, m + h))
			}
			
			FOURTH -> when (rand.nextInt(0, 5)) {
				in 0..1 -> MOBS(undreads = 1 + rand.nextInt(h, 3 + h), spiderlings = rand.nextRounded(0.4F * m) + rand.nextInt(0, m + h))
				else    -> MOBS(undreads = 2 + rand.nextInt(m, 2 * m), spiderlings = rand.nextRounded(0.3F + (m * 0.1F) + (h * 0.3F)))
			}
			
			else -> when (rand.nextInt(0, 2)) {
				0    -> MOBS(undreads = 1 + h + rand.nextInt(m, 1 + (m * 2)) + rand.nextInt(h, 1 + h), spiderlings = rand.nextInt(0, m))
				else -> MOBS(undreads = 2 + h + rand.nextInt(m, 2 * m),                                spiderlings = rand.nextRounded(0.2F + (m * 0.2F)))
			}
		}
	}
	
	private fun MOBS(undreads: Int, spiderlings: Int) = undreads to spiderlings
	
	enum class MobAmount {
		LOW, MEDIUM, HIGH
	}
}
