package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.system.random.removeItem
import chylex.hee.util.math.PosXZ
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST

class ObsidianTowerRoom_Chest_MarketStalls(file: String) : ObsidianTowerRoom_General(file) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		
		val remainingShelves = mutableListOf(
			PosXZ(1, centerZ - 1) to EAST,
			PosXZ(1, centerZ + 1) to EAST,
			PosXZ(maxX - 1, centerZ - 1) to WEST,
			PosXZ(maxX - 1, centerZ + 1) to WEST
		).apply {
			shuffle(rand)
		}
		
		rand.removeItem(remainingShelves).let { (pos, facing) ->
			placeChest(world, pos.withY(2), facing, isSpecial = true)
		}
		
		for ((pos, facing) in remainingShelves) {
			if (rand.nextInt(5) <= 1) {
				placeEndermanHead(world, pos.withY(2), facing)
			}
			else if (rand.nextInt(10) != 0) {
				placeFlowerPot(world, pos.withY(2))
			}
		}
	}
}
