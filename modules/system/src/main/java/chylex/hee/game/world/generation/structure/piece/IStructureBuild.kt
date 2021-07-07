package chylex.hee.game.world.generation.structure.piece

import chylex.hee.game.world.generation.structure.IStructureGenerator
import chylex.hee.util.math.BoundingBox

interface IStructureBuild : IStructureGenerator {
	val boundingBoxes: Iterable<BoundingBox>
}
