package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.migration.vanilla.BlockFence
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.with

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
			val extensionState = Blocks.DARK_OAK_FENCE.with(BlockFence.NORTH, true).with(BlockFence.SOUTH, true)
			
			repeat(rand.nextInt(rand.nextInt(3, 4), 5)){
				val pos = rand.removeItem(extensionPillars).withY(rand.removeItem(extensionHeights))
				val posN = pos.north()
				val posS = pos.south()
				
				world.setState(pos, extensionState)
				
				if (world.getBlock(posN) === Blocks.DARK_OAK_FENCE){
					world.setState(posN, world.getState(posN).with(BlockFence.SOUTH, true))
				}
				
				if (world.getBlock(posS) === Blocks.DARK_OAK_FENCE){
					world.setState(posS, world.getState(posS).with(BlockFence.NORTH, true))
				}
			}
		}
	}
}
