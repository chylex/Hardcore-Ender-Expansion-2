package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.SOUTH

class ObsidianTowerRoom_Rare_SpawnerDoor(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeLootTrigger(world, Pos(maxX - 1, 2, 4), isSpecial = true)
		
		placeEndermanHead(world, Pos(centerX - 2, 3, 2), SOUTH)
		placeEndermanHead(world, Pos(centerX + 2, 3, 2), SOUTH)
		
		for (y in 1..3) for (x in (centerX - 1)..(centerX + 1)) {
			placeSpawner(world, Pos(x, y, centerZ + 2), instance)
		}
	}
}
