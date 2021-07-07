package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos

class ObsidianTowerRoom_Regular_BloodyDroppers(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeSpawner(world, Pos(centerX, 1, centerZ), instance)
	}
}
