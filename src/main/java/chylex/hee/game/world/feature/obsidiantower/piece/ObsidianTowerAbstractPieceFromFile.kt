package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.StructurePiece

abstract class ObsidianTowerAbstractPieceFromFile(file: String) : StructurePiece(), IStructurePieceFromFile by Delegate("obsidiantower/$file", ObsidianTowerPieces.PALETTE){
	override val connections = emptyArray<IStructurePieceConnection>()
	
	override fun generate(world: IStructureWorld, instance: Instance){
		generator.generate(world)
	}
}
