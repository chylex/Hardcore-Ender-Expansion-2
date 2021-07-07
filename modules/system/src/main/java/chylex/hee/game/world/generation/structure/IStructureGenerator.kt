package chylex.hee.game.world.generation.structure

import chylex.hee.util.math.Size

interface IStructureGenerator {
	val size: Size
	
	fun generate(world: IStructureWorld)
}
