package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.NORTH

object TombDungeonStart : TombDungeonAbstractPiece(){
	override val size = Size(5, 5, 1)
	override val isFancy = false
	
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 0
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){}
}
