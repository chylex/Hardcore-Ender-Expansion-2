package chylex.hee.game.world.feature.obsidiantower
import chylex.hee.game.mechanics.potion.PotionBase.Companion.INFINITE_DURATION
import chylex.hee.system.util.nextItem
import net.minecraft.init.MobEffects.FIRE_RESISTANCE
import net.minecraft.init.MobEffects.REGENERATION
import net.minecraft.init.MobEffects.RESISTANCE
import net.minecraft.init.MobEffects.SPEED
import net.minecraft.init.MobEffects.STRENGTH
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import java.util.Random

enum class ObsidianTowerSpawnerLevel(
	val baseCooldown: Int,
	val mobLimitPerSpawner: IntRange,
	val mobLimitInSpawnArea: Int,
	private val effects: Array<Array<Pair<Potion, Int>>>
){
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
			arrayOf(SPEED to 1, STRENGTH to 1, FIRE_RESISTANCE to 1)
		)
	),
	
	LEVEL_3(
		baseCooldown = 12,
		mobLimitPerSpawner = 7..11,
		mobLimitInSpawnArea = 4,
		effects = arrayOf(
			arrayOf(SPEED to 1, STRENGTH to 1, RESISTANCE to 1, FIRE_RESISTANCE to 1),
			arrayOf(SPEED to 2, STRENGTH to 2)
		)
	),
	
	LEVEL_4(
		baseCooldown = 9,
		mobLimitPerSpawner = 12..17,
		mobLimitInSpawnArea = 5,
		effects = arrayOf(
			arrayOf(FIRE_RESISTANCE to 1),
			arrayOf(SPEED to 2, STRENGTH to 2, RESISTANCE to 1, FIRE_RESISTANCE to 1),
			arrayOf(SPEED to 3, RESISTANCE to 2, REGENERATION to 1)
		)
	);
	
	fun generateEffects(rand: Random): Collection<PotionEffect>{
		if (effects.isEmpty()){
			return emptyList()
		}
		
		val picks = mutableMapOf<Potion, PotionEffect>()
		
		for(list in effects){
			val (potion, level) = rand.nextItem(list)
			picks[potion] = PotionEffect(potion, INFINITE_DURATION, level - 1)
		}
		
		return picks.values
	}
}
