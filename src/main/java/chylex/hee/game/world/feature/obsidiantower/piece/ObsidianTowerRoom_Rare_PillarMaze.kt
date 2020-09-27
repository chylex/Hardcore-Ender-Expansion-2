package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Rare_PillarMaze(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(3, 4, 8), isSpecial = true)
		
		placeSpawner(world, Pos(1, 1, 3), instance)
		placeSpawner(world, Pos(2, 2, 6), instance)
		placeSpawner(world, Pos(4, 3, 4), instance)
		placeSpawner(world, Pos(4, 3, 8), instance)
		placeSpawner(world, Pos(6, 2, 6), instance)
		placeSpawner(world, Pos(7, 5, 2), instance)
		placeSpawner(world, Pos(8, 3, 4), instance)
	}
}
