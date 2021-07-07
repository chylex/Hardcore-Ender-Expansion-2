package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.game.world.generation.structure.world.OffsetStructureWorld
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MIN

class ObsidianTowerDebugRoomPiece(private val level: ObsidianTowerLevel_General, private val room: ObsidianTowerRoom_General) : StructurePiece<Unit>() {
	override val connections = emptyArray<IStructurePieceConnection>()
	
	override val size
		get() = level.size
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		val floorPos = size.getPos(CENTER, MIN, CENTER)
		
		world.placeCube(size.minPos, size.maxPos, Air)
		level.generate(OffsetStructureWorld(world, floorPos.subtract(level.size.getPos(CENTER, MIN, CENTER))))
		room.generate(OffsetStructureWorld(world, floorPos.subtract(room.size.getPos(CENTER, MIN, CENTER))))
	}
}
