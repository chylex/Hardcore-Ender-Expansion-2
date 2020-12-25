package chylex.hee.game.world.feature.energyshrine.connection

import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnectionType
import chylex.hee.init.ModBlocks

enum class EnergyShrineConnectionType : IStructurePieceConnectionType {
	CORRIDOR {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != TERMINAL
	},
	
	STAIR_BOTTOM {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = false // force stairs to always go down
	},
	
	STAIR_MIDDLE {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == STAIR_MIDDLE
	},
	
	STAIR_TOP {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == ROOM
	},
	
	ROOM {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR || target == ROOM || target == STAIR_BOTTOM
	},
	
	TERMINAL {
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != TERMINAL
	};
	
	override fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection) {
		val offset = connection.offset
		val perpendicular = connection.facing.rotateY()
		
		val addX = perpendicular.xOffset
		val addZ = perpendicular.zOffset
		
		world.placeCube(offset, offset.add(addX, 0, addZ), Single(ModBlocks.GLOOMROCK))
		world.placeCube(offset.add(0, 1, 0), offset.add(addX, 3, addZ), Air)
	}
}
