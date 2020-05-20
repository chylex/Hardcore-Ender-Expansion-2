package chylex.hee.game.world.feature.tombdungeon.piece

abstract class TombDungeonRoom(file: String, isFancy: Boolean) : TombDungeonAbstractPieceFromFile(file, isFancy){
	override val sidePathAttachWeight = 5
	override val secretAttachWeight = 0
}
