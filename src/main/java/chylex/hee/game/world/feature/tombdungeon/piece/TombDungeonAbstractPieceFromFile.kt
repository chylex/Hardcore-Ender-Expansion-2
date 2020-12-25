package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld

abstract class TombDungeonAbstractPieceFromFile(file: String, override val isFancy: Boolean) : TombDungeonAbstractPiece(), IStructurePieceFromFile by Delegate("tombdungeon/$file", if (isFancy) TombDungeonPieces.PALETTE_FANCY else TombDungeonPieces.PALETTE) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		generator.generate(world)
		placeCobwebs(world, instance)
	}
}
