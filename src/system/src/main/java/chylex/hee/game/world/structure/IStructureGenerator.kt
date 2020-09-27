package chylex.hee.game.world.structure
import chylex.hee.game.world.math.Size

interface IStructureGenerator{
	val size: Size
	
	fun generate(world: IStructureWorld)
}
