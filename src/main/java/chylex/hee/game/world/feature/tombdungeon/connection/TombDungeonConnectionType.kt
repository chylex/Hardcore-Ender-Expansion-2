package chylex.hee.game.world.feature.tombdungeon.connection
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnectionType

enum class TombDungeonConnectionType : IStructurePieceConnectionType{
	CORRIDOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = true
	},
	
	STAIR_BOTTOM{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = false // force stairs to always go down
		override fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection){}
	},
	
	STAIR_TOP{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR || target == STAIR_BOTTOM
		override fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection){}
	},
	
	TOMB_ENTRANCE{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == TOMB_ENTRANCE
		
		override fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection){
			val offset = connection.offset
			world.setAir(offset.up(1))
			world.setAir(offset.up(2))
		}
	},
	
	ROOM_ENTRANCE{
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
