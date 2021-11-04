package chylex.hee.game.world.generation.feature.obsidiantower

import chylex.hee.game.potion.brewing.PotionBrewing.INFINITE_DURATION
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.util.random.nextItem
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.Effects
import java.util.Random

enum class ObsidianTowerSpawnerLevel(
	val baseCooldown: Int,
	val mobLimitPerSpawner: IntRange,
	val mobLimitInSpawnArea: Int,
	private val effects: Array<Array<Pair<Effect, Int>>>,
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
				Effects.SPEED to 1,
				Effects.STRENGTH to 1,
				Effects.FIRE_RESISTANCE to 1
			)
		)
	),
	
	LEVEL_3(
		baseCooldown = 12,
		mobLimitPerSpawner = 7..11,
		mobLimitInSpawnArea = 4,
		effects = arrayOf(
			arrayOf(
				Effects.SPEED to 1,
				Effects.STRENGTH to 1,
				Effects.RESISTANCE to 1,
				Effects.FIRE_RESISTANCE to 1
			),
			arrayOf(
				Effects.SPEED to 2,
				Effects.STRENGTH to 2
			)
		)
	),
	
	LEVEL_4(
		baseCooldown = 9,
		mobLimitPerSpawner = 12..17,
		mobLimitInSpawnArea = 5,
		effects = arrayOf(
			arrayOf(
				Effects.FIRE_RESISTANCE to 1
			),
			arrayOf(
				Effects.SPEED to 2,
				Effects.STRENGTH to 2,
				Effects.RESISTANCE to 1
			),
			arrayOf(
				Effects.SPEED to 3,
				Effects.RESISTANCE to 2,
				Effects.REGENERATION to 1
			)
		)
	);
	
	fun generateEffects(rand: Random): Collection<EffectInstance> {
		if (effects.isEmpty()) {
			return emptyList()
		}
		
		val picks = mutableMapOf<Effect, EffectInstance>()
		
		for (list in effects) {
			val (potion, level) = rand.nextItem(list)
			picks[potion] = potion.makeInstance(INFINITE_DURATION, level - 1)
		}
		
		return picks.values
	}
}
