package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt

class ObsidianTowerRoom_Regular_IronSupports(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		when (world.rand.nextInt(0, if (instance.context?.spawnerLevel == LEVEL_1) 1 else 3)) {
			0 -> placeSpawner(world, Pos(1, maxY - 1, centerZ), instance)
			1 -> placeSpawner(world, Pos(maxX - 1, maxY - 1, centerZ), instance)
		}
	}
}
