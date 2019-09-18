package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.removeItem

class ObsidianTowerRoom_Regular_SurroundFenceSpawners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		val rand = world.rand
		
		val spawnerPillars = mutableListOf(
			PosXZ(1, centerZ),
			PosXZ(maxX - 1, centerZ),
			PosXZ(centerX - 2, 1),
			PosXZ(centerX + 2, 1)
		)
		
		repeat(rand.nextInt(2, 4)){
			placeSpawner(world, rand.removeItem(spawnerPillars).withY(rand.nextInt(1, maxY - 1)), instance)
		}
	}
}
