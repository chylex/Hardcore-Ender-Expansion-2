package chylex.hee.game.world.feature.stronghold.connection
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnectionType

enum class StrongholdConnectionType : IStructurePieceConnectionType{
	CORRIDOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = true
	},
	
	STAIR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != DOOR
	},
	
	DOOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != DOOR && target != STAIR
	},
	
	ROOM{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR || target == DOOR
	},
	
	DEAD_END{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR
	};
	
	override fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection){
		val offset = connection.offset
		val perpendicular = connection.facing.rotateY()
		
		val addX = perpendicular.xOffset
		val addZ = perpendicular.zOffset
		
		world.placeCube(offset.add(-addX, 1, -addZ), offset.add(addX, 3, addZ), Air)
	}
}
