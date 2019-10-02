package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Regular_GuardingHeads(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeEndermanHead(world, Pos(centerX - 2, 2, centerZ - 2), EAST)
		placeEndermanHead(world, Pos(centerX - 2, 2, centerZ + 2), EAST)
		
		placeEndermanHead(world, Pos(centerX + 2, 2, centerZ - 2), WEST)
		placeEndermanHead(world, Pos(centerX + 2, 2, centerZ + 2), WEST)
		
		placeSpawner(world, Pos(1, 1, centerZ), instance)
		placeSpawner(world, Pos(maxX - 1, 1, centerZ), instance)
	}
}
