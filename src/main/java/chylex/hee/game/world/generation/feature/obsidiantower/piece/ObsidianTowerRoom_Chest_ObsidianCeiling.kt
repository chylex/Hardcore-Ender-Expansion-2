package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos

class ObsidianTowerRoom_Chest_ObsidianCeiling(file: String) : ObsidianTowerRoom_General(file) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeLootTrigger(world, Pos(centerX, 1, centerZ), isSpecial = true)
	}
}
