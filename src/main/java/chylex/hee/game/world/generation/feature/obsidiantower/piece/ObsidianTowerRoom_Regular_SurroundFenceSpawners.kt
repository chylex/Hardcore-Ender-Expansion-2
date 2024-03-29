package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.PosXZ
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.removeItem

class ObsidianTowerRoom_Regular_SurroundFenceSpawners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		
		val spawnerPillars = mutableListOf(
			PosXZ(1, centerZ),
			PosXZ(maxX - 1, centerZ),
			PosXZ(centerX - 2, 1),
			PosXZ(centerX + 2, 1)
		)
		
		repeat(rand.nextInt(2, 4)) {
			placeSpawner(world, rand.removeItem(spawnerPillars).withY(rand.nextInt(1, maxY - 1)), instance)
		}
	}
}
