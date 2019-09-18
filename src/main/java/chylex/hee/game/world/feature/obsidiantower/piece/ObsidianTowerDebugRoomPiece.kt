package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MIN

class ObsidianTowerDebugRoomPiece(private val level: ObsidianTowerLevel_General, private val room: ObsidianTowerRoom_General) : StructurePiece<Unit>(){
	override val connections = emptyArray<IStructurePieceConnection>()
	
	override val size
		get() = level.size
	
	override fun generate(world: IStructureWorld, instance: Instance){
		val floorPos = size.getPos(CENTER, MIN, CENTER)
		
		world.placeCube(size.minPos, size.maxPos, Air)
		level.generate(OffsetStructureWorld(world, floorPos.subtract(level.size.getPos(CENTER, MIN, CENTER))))
		room.generate(OffsetStructureWorld(world, floorPos.subtract(room.size.getPos(CENTER, MIN, CENTER))))
	}
}
