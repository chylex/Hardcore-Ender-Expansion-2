package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.WEST

class ObsidianTowerRoom_Chest_SpiralStaircase(file: String) : ObsidianTowerRoom_General(file) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeLootTrigger(world, Pos(maxX - 1, 4, 3), isSpecial = true)
		
		for (z in 4..6) {
			placeEndermanHead(world, Pos(maxX - 1, 2, z), WEST)
		}
	}
}
