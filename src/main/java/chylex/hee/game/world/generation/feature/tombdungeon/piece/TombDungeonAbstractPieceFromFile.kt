package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile.Delegate

abstract class TombDungeonAbstractPieceFromFile(file: String, override val isFancy: Boolean) : TombDungeonAbstractPiece(), IStructurePieceFromFile by Delegate("tombdungeon/$file", if (isFancy) TombDungeonPieces.PALETTE_FANCY else TombDungeonPieces.PALETTE) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		generator.generate(world)
		placeCobwebs(world, instance)
	}
}
