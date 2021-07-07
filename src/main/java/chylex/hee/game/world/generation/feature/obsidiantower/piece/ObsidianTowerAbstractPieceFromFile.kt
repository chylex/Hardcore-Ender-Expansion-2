package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.structure.piece.StructurePiece

abstract class ObsidianTowerAbstractPieceFromFile<T>(file: String) : StructurePiece<T>(), IStructurePieceFromFile by Delegate("obsidiantower/$file", ObsidianTowerPieces.PALETTE) {
	override val connections = emptyArray<IStructurePieceConnection>()
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		generator.generate(world)
	}
}
