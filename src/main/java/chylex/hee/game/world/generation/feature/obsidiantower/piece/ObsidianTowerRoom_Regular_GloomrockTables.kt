package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import chylex.hee.util.math.PosXZ
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import chylex.hee.util.random.removeItem
import net.minecraft.block.Blocks
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST

class ObsidianTowerRoom_Regular_GloomrockTables(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		
		placeEndermanHead(world, Pos(1, 4, centerZ - 2), EAST)
		placeEndermanHead(world, Pos(1, 4, centerZ + 2), EAST)
		
		placeEndermanHead(world, Pos(maxX - 1, 4, centerZ - 2), WEST)
		placeEndermanHead(world, Pos(maxX - 1, 4, centerZ + 2), WEST)
		
		val xPositions = intArrayOf(1, maxX - 1)
		val spawnerOverride = if (instance.context?.spawnerLevel == LEVEL_1) rand.nextItem(xPositions, 0) else 0
		
		for (x in xPositions) {
			val pos = Pos(x, 1, centerZ)
			
			if (x == spawnerOverride) {
				placeSpawner(world, pos, instance)
			}
			else when (rand.nextInt(0, 5)) {
				0       -> world.setBlock(pos, Blocks.CRAFTING_TABLE)
				in 1..2 -> placeFurnace(world, pos, if (x < centerX) EAST else WEST)
				else    -> placeSpawner(world, pos, instance)
			}
		}
		
		val tablePlaces = arrayOf(
			PosXZ(-4, -2),
			PosXZ(-4, -1),
			PosXZ(-4, +1),
			PosXZ(-4, +2)
		).flatMap {
			listOf(it, it.copy(x = -it.x))
		}.toMutableList()
		
		repeat(rand.nextInt(1, 3)) {
			placeFlowerPot(world, rand.removeItem(tablePlaces).add(centerX, centerZ).withY(2))
		}
	}
}
