package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos

class ObsidianTowerRoom_Regular_CeilingSpawners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeSpawner(world, Pos(3, maxY - 3, 3), instance)
		placeSpawner(world, Pos(2, maxY - 2, maxZ - 3), instance)
		placeSpawner(world, Pos(maxX - 2, 2, maxZ - 3), instance)
		placeSpawner(world, Pos(maxX - 2, maxY - 2, 4), instance)
	}
}
