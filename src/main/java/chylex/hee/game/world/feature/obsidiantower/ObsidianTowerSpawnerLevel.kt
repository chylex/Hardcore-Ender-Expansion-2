package chylex.hee.game.world.feature.obsidiantower

import chylex.hee.game.potion.brewing.PotionBrewing.INFINITE_DURATION
import chylex.hee.game.potion.makeEffect
import chylex.hee.system.migration.Potion
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.nextItem
import net.minecraft.potion.EffectInstance
import java.util.Random

enum class ObsidianTowerSpawnerLevel(
	val baseCooldown: Int,
	val mobLimitPerSpawner: IntRange,
	val mobLimitInSpawnArea: Int,
	private val effects: Array<Array<Pair<Potion, Int>>>,
) {
	INTRODUCTION(
		baseCooldown = 0,
		mobLimitPerSpawner = IntRange.EMPTY,
		mobLimitInSpawnArea = 2,
		effects = emptyArray()
	),
	
	LEVEL_1(
		baseCooldown = 15,
		mobLimitPerSpawner = 2..4,
		mobLimitInSpawnArea = 2,
		effects = emptyArray()
	),
	
	LEVEL_2(
		baseCooldown = 14,
		mobLimitPerSpawner = 3..6,
		mobLimitInSpawnArea = 3,
		effects = arrayOf(
			arrayOf(
				Potions.SPEED to 1,
				Potions.STRENGTH to 1,
				Potions.FIRE_RESISTANCE to 1
			)
		)
	),
	
	LEVEL_3(
		baseCooldown = 12,
		mobLimitPerSpawner = 7..11,
		mobLimitInSpawnArea = 4,
		effects = arrayOf(
			arrayOf(
				Potions.SPEED to 1,
				Potions.STRENGTH to 1,
				Potions.RESISTANCE to 1,
				Potions.FIRE_RESISTANCE to 1
			),
			arrayOf(
				Potions.SPEED to 2,
				Potions.STRENGTH to 2
			)
		)
	),
	
	LEVEL_4(
		baseCooldown = 9,
		mobLimitPerSpawner = 12..17,
		mobLimitInSpawnArea = 5,
		effects = arrayOf(
			arrayOf(
				Potions.FIRE_RESISTANCE to 1
			),
			arrayOf(
				Potions.SPEED to 2,
				Potions.STRENGTH to 2,
				Potions.RESISTANCE to 1
			),
			arrayOf(
				Potions.SPEED to 3,
				Potions.RESISTANCE to 2,
				Potions.REGENERATION to 1
			)
		)
	);
	
	fun generateEffects(rand: Random): Collection<EffectInstance> {
		if (effects.isEmpty()) {
			return emptyList()
		}
		
		val picks = mutableMapOf<Potion, EffectInstance>()
		
		for(list in effects) {
			val (potion, level) = rand.nextItem(list)
			picks[potion] = potion.makeEffect(INFINITE_DURATION, level - 1)
		}
		
		return picks.values
	}
}
