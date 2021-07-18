package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST

class ObsidianTowerRoom_Regular_GuardingHeads(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeEndermanHead(world, Pos(centerX - 2, 2, centerZ - 2), EAST)
		placeEndermanHead(world, Pos(centerX - 2, 2, centerZ + 2), EAST)
		
		placeEndermanHead(world, Pos(centerX + 2, 2, centerZ - 2), WEST)
		placeEndermanHead(world, Pos(centerX + 2, 2, centerZ + 2), WEST)
		
		placeSpawner(world, Pos(1, 1, centerZ), instance)
		placeSpawner(world, Pos(maxX - 1, 1, centerZ), instance)
	}
}
