package chylex.hee.game.world.generation.feature.obsidiantower

import java.util.Random

class ObsidianTowerRoomData(val spawnerLevel: ObsidianTowerSpawnerLevel, rand: Random) {
	val effects = spawnerLevel.generateEffects(rand)
}
