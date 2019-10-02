package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Regular_LibraryCorners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		val rand = world.rand
		
		placeSpawner(world, Pos(1, centerY, centerZ), instance)
		placeSpawner(world, Pos(maxX - 1, centerY, centerZ), instance)
		
		val x = if (rand.nextBoolean()) 1 else maxX - 1
		val z = if (rand.nextBoolean()) centerZ - 1 else centerZ + 1
		
		if (rand.nextInt(3) != 0){
			placeChest(world, Pos(x, 2, z), if (x < centerX) EAST else WEST)
		}
		else{
			placeFlowerPot(world, Pos(x, 2, z))
		}
	}
}
