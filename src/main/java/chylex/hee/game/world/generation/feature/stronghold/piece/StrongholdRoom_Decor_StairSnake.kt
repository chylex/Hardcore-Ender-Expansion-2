package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt

class StrongholdRoom_Decor_StairSnake(file: String) : StrongholdRoom_Decor_Generic(file) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		repeat(5) {
			val cobwebPos = Pos(
				rand.nextInt(1, maxX - 1),
				rand.nextInt(maxY - 2, maxY - 1),
				rand.nextInt(1, maxZ - 1)
			)
			
			if (world.isAir(cobwebPos)) {
				world.setBlock(cobwebPos, ModBlocks.ANCIENT_COBWEB)
			}
		}
	}
}
