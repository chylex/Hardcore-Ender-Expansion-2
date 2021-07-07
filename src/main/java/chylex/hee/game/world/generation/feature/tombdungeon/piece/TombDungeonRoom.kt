package chylex.hee.game.world.generation.feature.tombdungeon.piece

abstract class TombDungeonRoom(file: String, isFancy: Boolean) : TombDungeonAbstractPieceFromFile(file, isFancy) {
	override val sidePathAttachWeight = 45
	override val secretAttachWeight = 0
}
