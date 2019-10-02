package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Rare_SpawnerDoor(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(maxX - 1, 2, 4), isSpecial = true)
		
		placeEndermanHead(world, Pos(centerX - 2, 3, 2), SOUTH)
		placeEndermanHead(world, Pos(centerX + 2, 3, 2), SOUTH)
		
		for(y in 1..3) for(x in (centerX - 1)..(centerX + 1)){
			placeSpawner(world, Pos(x, y, centerZ + 2), instance)
		}
	}
}
