package chylex.hee.game.world.structure
import chylex.hee.game.world.structure.palette.Palette

interface IStructurePieceFromFile : IStructurePiece{
	val path: String
	val palette: Palette
	val generator: IStructureGenerator
	
	@JvmDefault val maxX get() = size.maxX
	@JvmDefault val maxY get() = size.maxY
	@JvmDefault val maxZ get() = size.maxZ
	@JvmDefault val centerX get() = size.centerX
	@JvmDefault val centerY get() = size.centerY
	@JvmDefault val centerZ get() = size.centerZ
	
	class Delegate(override val path: String, override val palette: Palette) : IStructurePieceFromFile{
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
