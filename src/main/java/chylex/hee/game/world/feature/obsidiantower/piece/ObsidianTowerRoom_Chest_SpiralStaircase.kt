package chylex.hee.game.world.feature.obsidiantower.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.WEST

class ObsidianTowerRoom_Chest_SpiralStaircase(file: String) : ObsidianTowerRoom_General(file) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeLootTrigger(world, Pos(maxX - 1, 4, 3), isSpecial = true)
		
		for(z in 4..6) {
			placeEndermanHead(world, Pos(maxX - 1, 2, z), WEST)
		}
	}
}
