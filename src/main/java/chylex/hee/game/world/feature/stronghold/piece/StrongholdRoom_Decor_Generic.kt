package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.ROOM

open class StrongholdRoom_Decor_Generic(file: String) : StrongholdAbstractPieceFromFile(file, ROOM){
	override val isEyeOfEnderTarget = true
}
