package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Regular_CeilingSpawners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeSpawner(world, Pos(3, maxY - 3, 3), instance)
		placeSpawner(world, Pos(2, maxY - 2, maxZ - 3), instance)
		placeSpawner(world, Pos(maxX - 2, 2, maxZ - 3), instance)
		placeSpawner(world, Pos(maxX - 2, maxY - 2, 4), instance)
	}
}
