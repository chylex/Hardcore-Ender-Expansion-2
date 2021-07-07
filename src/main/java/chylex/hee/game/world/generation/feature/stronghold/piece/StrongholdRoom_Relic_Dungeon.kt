package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.SKULL_ROTATION
import chylex.hee.game.block.util.with
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.system.random.nextInt
import chylex.hee.util.math.Pos
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack

class StrongholdRoom_Relic_Dungeon(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem) {
	override val lootChestPos = Pos(centerX, 2, 2)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextBoolean()) {
			world.setState(Pos(centerX - 1, 2, 2), Blocks.ZOMBIE_HEAD.with(SKULL_ROTATION, 7))
		}
		else {
			world.setState(Pos(centerX + 1, 2, 2), Blocks.ZOMBIE_HEAD.with(SKULL_ROTATION, 9))
		}
		
		repeat(rand.nextInt(9, 15)) {
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(3, 10))
			
			if (world.isAir(redstonePos)) {
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
