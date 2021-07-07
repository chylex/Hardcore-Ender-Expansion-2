package chylex.hee.game.world.generation.structure.file

import chylex.hee.game.world.generation.structure.IStructureGenerator
import chylex.hee.game.world.generation.structure.IStructurePiece
import chylex.hee.game.world.generation.structure.palette.Palette

interface IStructurePieceFromFile : IStructurePiece {
	val path: String
	val palette: Palette
	val generator: IStructureGenerator
	
	val maxX get() = size.maxX
	val maxY get() = size.maxY
	val maxZ get() = size.maxZ
	val centerX get() = size.centerX
	val centerY get() = size.centerY
	val centerZ get() = size.centerZ
	
	class Delegate(override val path: String, override val palette: Palette) : IStructurePieceFromFile {
		override val generator = StructureFiles.loadWithCache(path).Generator(palette.mappingForGeneration)
		override val size = generator.size
		
		override val maxX = size.maxX
		override val maxY = size.maxY
		override val maxZ = size.maxZ
		override val centerX = size.centerX
		override val centerY = size.centerY
		override val centerZ = size.centerZ
	}
}
