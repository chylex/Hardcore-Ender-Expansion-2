package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.math.BoundingBox
import chylex.hee.game.world.structure.IStructureGenerator

interface IStructureBuild : IStructureGenerator{
	val boundingBoxes: Iterable<BoundingBox>
}
