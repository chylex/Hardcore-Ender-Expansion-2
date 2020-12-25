package chylex.hee.game.world.feature.stronghold.piece

import chylex.hee.game.world.feature.stronghold.StrongholdPieceType

open class StrongholdRoom_Decor_Generic(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override val isEyeOfEnderTarget = true
}
