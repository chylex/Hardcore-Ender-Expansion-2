package chylex.hee.game.world.generation.feature.energyshrine

import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces.PIECES_END
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces.PIECES_START
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces.STRUCTURE_SIZE
import chylex.hee.game.world.generation.feature.energyshrine.piece.EnergyShrineAbstractPiece
import chylex.hee.game.world.generation.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180_Bottom
import chylex.hee.game.world.generation.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180_Top
import chylex.hee.game.world.generation.structure.piece.IStructureBuild
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder.ProcessBase
import chylex.hee.game.world.generation.structure.piece.StructureBuild
import chylex.hee.game.world.generation.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.generation.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.game.world.util.Transform
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MAX
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import chylex.hee.util.random.nextItemOrNull
import chylex.hee.util.random.removeItemOrNull
import java.util.Random

object EnergyShrineBuilder : IStructureBuilder<IStructureBuild> {
	override fun build(rand: Random): IStructureBuild? {
		val remainingRooms = EnergyShrinePieces.generateRoomConfiguration(rand, targetMainPathRoomAmount = rand.nextInt(3, 4))
		val remainingCorridors = EnergyShrinePieces.generateCorridorConfiguration(rand, remainingRooms)
		
		val startingPiece = rand.nextItem(PIECES_START).MutableInstance(EnergyShrineRoomData.DEFAULT, rand.nextItem(Transform.ALL))
		val startingPiecePos = STRUCTURE_SIZE.getPos(CENTER, MAX, CENTER).subtract(startingPiece.size.getPos(CENTER, MAX, CENTER)).down()
		
		val build = StructureBuild(STRUCTURE_SIZE, PositionedPiece(startingPiece, startingPiecePos))
		val process = Process(build, rand)
		
		// main path rooms
		
		while (true) {
			val nextGeneratedPiece = rand.removeItemOrNull(remainingRooms.mainPath) ?: break
			
			if (!build.guardChain(30) { process.placeRoom(remainingCorridors.mainPath, build.lastPiece, nextGeneratedPiece) }) {
				return null
			}
		}
		
		// final room
		
		run {
			val finalRoomPiece = rand.nextItem(PIECES_END) to EnergyShrineRoomData.DEFAULT
			
			if (!build.guardChain(30) { process.placeRoom(remainingCorridors.mainPath, build.lastPiece, finalRoomPiece) }) {
				return null
			}
		}
		
		// off path rooms
		
		while (true) {
			val nextGeneratedPiece = rand.removeItemOrNull(remainingRooms.offPath) ?: break
			
			if (!build.guardChain(30) { process.placeRoom(remainingCorridors.offPath, rand.nextItem(build.generatedPieces), nextGeneratedPiece) }) {
				return null
			}
		}
		
		return build.freeze()
	}
	
	private class Process(build: StructureBuild<StructurePiece<EnergyShrineRoomData>.MutableInstance>, rand: Random) : ProcessBase<StructurePiece<EnergyShrineRoomData>.MutableInstance>(build, rand) {
		
		// Piece placement
		
		fun placeRoom(corridorList: MutableList<Array<out EnergyShrineAbstractPiece>>, targetPiece: PositionedPiece<StructurePiece<EnergyShrineRoomData>.MutableInstance>, generatedPiece: Pair<EnergyShrineAbstractPiece, EnergyShrineRoomData>): Boolean {
			val corridorChain = rand.nextItem(corridorList)
			var lastPiece = targetPiece
			
			lateinit var stairTransform: Transform
			
			for (corridorPiece in corridorChain) {
				lastPiece = when (corridorPiece) {
					is EnergyShrineCorridor_Staircase_180_Top    -> appendPiece(lastPiece) { stairTransform = it; corridorPiece.MutableInstance(it) }
					is EnergyShrineCorridor_Staircase_180_Bottom -> appendPiece(lastPiece) { corridorPiece.MutableInstance(stairTransform) }
					else                                         -> appendPiece(lastPiece, corridorPiece::MutableInstance)
				} ?: return false
			}
			
			if (appendPiece(lastPiece) { generatedPiece.first.MutableInstance(generatedPiece.second, it) } == null) {
				return false
			}
			
			corridorList.remove(corridorChain)
			return true
		}
		
		private fun appendPiece(targetPiece: PositionedPiece<StructurePiece<EnergyShrineRoomData>.MutableInstance>, generatedPieceConstructor: (Transform) -> StructurePiece<EnergyShrineRoomData>.MutableInstance): PositionedPiece<StructurePiece<EnergyShrineRoomData>.MutableInstance>? {
			val targetConnection = rand.nextItemOrNull(targetPiece.instance.findAvailableConnections()) ?: return null
			return baseAddPiece(APPEND, targetPiece, targetConnection, generatedPieceConstructor)
		}
	}
}
