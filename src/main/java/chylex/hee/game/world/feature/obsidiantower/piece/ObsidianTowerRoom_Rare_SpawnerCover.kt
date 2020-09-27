package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Rare_SpawnerCover(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(centerX, 1, centerZ + 1), isSpecial = true)
		
		placeSpawner(world, Pos(centerX, 1, centerZ), instance)
		placeSpawner(world, Pos(centerX, 2, centerZ + 1), instance)
		placeSpawner(world, Pos(centerX - 1, 1, centerZ + 1), instance)
		placeSpawner(world, Pos(centerX + 1, 1, centerZ + 1), instance)
	}
}
