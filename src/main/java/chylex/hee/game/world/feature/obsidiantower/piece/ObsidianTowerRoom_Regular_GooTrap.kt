package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Regular_GooTrap(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeSpawner(world, Pos(1, 2, centerZ), instance)
		placeSpawner(world, Pos(centerX, 2, centerZ - 1), instance)
		placeSpawner(world, Pos(maxX - 1, 2, centerZ), instance)
	}
}
