package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.removeItem

class ObsidianTowerRoom_Regular_SideFenceSpawners(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		val rand = world.rand
		
		run {
			val spawnerPillars = mutableListOf(
				PosXZ(1, centerZ - 1),
				PosXZ(1, centerZ + 1),
				PosXZ(maxX - 1, centerZ - 1),
				PosXZ(maxX - 1, centerZ + 1)
			)
			
			val spawnerHeights = (1 until maxY).toMutableList()
			
			repeat(rand.nextInt(2, 4)){
				placeSpawner(world, rand.removeItem(spawnerPillars).withY(rand.removeItem(spawnerHeights)), instance)
			}
		}
		
		run {
			val extensionPillars = mutableListOf(
				PosXZ(1, centerZ - 2),
				PosXZ(1, centerZ),
				PosXZ(1, centerZ + 2),
				PosXZ(maxX - 1, centerZ - 2),
				PosXZ(maxX - 1, centerZ),
				PosXZ(maxX - 1, centerZ + 2)
			)
			
			val extensionHeights = (1 until maxY).toMutableList()
			
			repeat(rand.nextInt(rand.nextInt(3, 4), 5)){
				world.setBlock(rand.removeItem(extensionPillars).withY(rand.removeItem(extensionHeights)), Blocks.DARK_OAK_FENCE)
			}
		}
	}
}
