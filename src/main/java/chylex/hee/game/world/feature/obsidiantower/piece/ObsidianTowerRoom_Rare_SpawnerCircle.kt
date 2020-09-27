package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Rare_SpawnerCircle(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(centerX, 2, centerZ), isSpecial = true)
		
		placeSpawner(world, Pos(3, 4, 3), instance)
		placeSpawner(world, Pos(2, 3, 5), instance)
		placeSpawner(world, Pos(3, 2, 7), instance)
		
		placeSpawner(world, Pos(maxX - 3, 4, 3), instance)
		placeSpawner(world, Pos(maxX - 2, 3, 5), instance)
		placeSpawner(world, Pos(maxX - 3, 2, 7), instance)
	}
}
