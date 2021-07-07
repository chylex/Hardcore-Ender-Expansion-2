package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType

open class StrongholdRoom_Decor_Generic(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override val isEyeOfEnderTarget = true
}
