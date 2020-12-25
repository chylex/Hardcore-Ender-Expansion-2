package chylex.hee.game.world.feature.obsidiantower

import java.util.Random

class ObsidianTowerRoomData(val spawnerLevel: ObsidianTowerSpawnerLevel, rand: Random) {
	val effects = spawnerLevel.generateEffects(rand)
}
