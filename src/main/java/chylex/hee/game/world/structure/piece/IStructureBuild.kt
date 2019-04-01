package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.util.BoundingBox

interface IStructureBuild : IStructureGenerator{
	val boundingBoxes: Sequence<BoundingBox>
}
