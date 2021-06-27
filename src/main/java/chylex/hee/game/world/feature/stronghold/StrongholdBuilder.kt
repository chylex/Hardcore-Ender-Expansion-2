package chylex.hee.game.world.feature.stronghold

import chylex.hee.HEE
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_CORRIDORS
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_CORRIDOR_DEAD_END
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_CORRIDOR_RELIC
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_DEAD_ENDS
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_DOORS
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_RELICS
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_ROOMS
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.PIECES_START
import chylex.hee.game.world.feature.stronghold.StrongholdPieces.STRUCTURE_SIZE
import chylex.hee.game.world.feature.stronghold.piece.StrongholdAbstractPiece
import chylex.hee.game.world.feature.stronghold.piece.StrongholdAbstractPiece.StrongholdInst
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.structure.piece.IStructureBuilder
import chylex.hee.game.world.structure.piece.IStructureBuilder.ProcessBase
import chylex.hee.game.world.structure.piece.StructureBuild
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.system.collection.MutableWeightedList
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.removeItemOrNull
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.max
import kotlin.math.min

object StrongholdBuilder : IStructureBuilder<IStructureBuild> {
	fun buildWithEyeOfEnderTarget(rand: Random): Pair<IStructureBuild, BlockPos?>? {
		val build = StructureBuild(STRUCTURE_SIZE, rand.nextItem(PIECES_START).StrongholdInst(distanceToPortal = 0, facingFromPortal = null, transform = rand.nextItem(Transform.ALL)))
		val process = Process(build, rand)
		
		// normal rooms
		
		run {
			val remainingRooms = PIECES_ROOMS(rand)
			val totalAttempts = remainingRooms.size * 5
			
			for(totalAttempt in 1..totalAttempts) {
				val nextTargetPiece = process.pickNextTargetPiece() ?: break
				val nextGeneratedPiece = remainingRooms.getOrNull(rand.nextInt(0, min(1, remainingRooms.size - 1))) ?: break
				
				if (build.guardChain(5) { process.placeNormalRoom(nextTargetPiece, nextGeneratedPiece, pathLength = rand.nextInt(1, 8)) }) {
					remainingRooms.remove(nextGeneratedPiece)
					
					if (remainingRooms.isEmpty()) {
						break
					}
				}
			}
			
			if (remainingRooms.isNotEmpty()) {
				HEE.log.debug("[StrongholdBuilder] failed at rooms")
				return null
			}
		}
		
		// relic rooms
		
		run {
			val openPiecesByDistance = build.generatedPieces.filter { it.instance.hasAvailableConnections }.sortedByDescending { it.instance.distanceToPortal }.toList()
			val remainingRelicTargets = openPiecesByDistance.take(max(10, openPiecesByDistance.size / 3)).toMutableList()
			val remainingRelicRooms = PIECES_RELICS(rand)
			
			var lastRelicFacingFromPortal: Direction? = null
			
			while(remainingRelicRooms.isNotEmpty()) {
				val targetPiece = rand.removeItemOrNull(remainingRelicTargets) ?: break
				val targetInstance = targetPiece.instance
				
				if (targetInstance.facingFromPortal != lastRelicFacingFromPortal) {
					val relicRoom = rand.nextItem(remainingRelicRooms)
					
					if (build.guardChain(5) { process.placeRelicRoom(targetPiece, relicRoom, pathLength = rand.nextInt(9, 10)) }) {
						remainingRelicRooms.remove(relicRoom)
						lastRelicFacingFromPortal = targetInstance.facingFromPortal
					}
				}
			}
			
			if (remainingRelicRooms.isNotEmpty()) {
				HEE.log.debug("[StrongholdBuilder] failed at relics")
				return null
			}
		}
		
		// dead ends
		
		run {
			val openPiecesForDeadEnds = build.generatedPieces.filter { it.instance.canLeadIntoDeadEnd }.shuffled(rand)
			val remainingDeadEnds = PIECES_DEAD_ENDS(rand)
			
			var lastDeadEndFacingFromPortal: Direction? = null
			var lastDeadEndFacingCounter = 0
			
			for(targetPiece in openPiecesForDeadEnds) {
				val targetFacing = targetPiece.instance.facingFromPortal
				
				if (targetFacing != lastDeadEndFacingFromPortal || lastDeadEndFacingCounter == 0) {
					val deadEnd = rand.nextItem(remainingDeadEnds)
					
					if (build.guardChain(5) { process.placeDeadEnd(targetPiece, deadEnd, pathLength = rand.nextInt(3, 7)) }) {
						remainingDeadEnds.remove(deadEnd)
						
						if (remainingDeadEnds.isEmpty()) {
							break
						}
						
						if (targetFacing == lastDeadEndFacingFromPortal) {
							++lastDeadEndFacingCounter
						}
						else {
							lastDeadEndFacingFromPortal = targetFacing
							lastDeadEndFacingCounter = 0
						}
					}
				}
			}
			
			if (remainingDeadEnds.isNotEmpty()) {
				HEE.log.debug("[StrongholdBuilder] failed at dead ends")
				return null
			}
		}
		
		// finish
		
		val generator = build.freeze()
		val eyeOfEnderTargets = build.generatedPieces.filter { it.instance.isEyeOfEnderTarget }.shuffled(rand)
		
		return generator to eyeOfEnderTargets.firstOrNull()?.pieceBox?.center?.subtract(STRUCTURE_SIZE.centerPos)
	}
	
	override fun build(rand: Random): IStructureBuild? {
		return buildWithEyeOfEnderTarget(rand)?.first
	}
	
	@Suppress("ReplaceSingleLineLet")
	private class Process(build: StructureBuild<StrongholdInst>, rand: Random) : ProcessBase<StrongholdInst>(build, rand) {
		
		// Piece picking
		
		fun pickNextTargetPiece(): PositionedPiece<StrongholdInst>? {
			val weightedPieces = build.generatedPieces.mapNotNull {
				val weight = it.instance.pickWeight
				if (weight == 0) null else weight to it
			}
			
			return weightedPieces.ifEmpty { null }?.let(::WeightedList)?.generateItem(rand)
		}
		
		// Piece placement
		
		fun placeNormalRoom(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, pathLength: Int): Boolean {
			val distanceToPortal = targetPiece.instance.distanceToPortal
			
			val firstPieceIsDoor = distanceToPortal > 0 && rand.nextInt(5) <= 2
			val lastPieceIsDoor = (!firstPieceIsDoor || pathLength > 3) && rand.nextInt(5) <= 2
			
			return targetPiece
				 .let { if (firstPieceIsDoor) addDoorPiece(it, MERGE) else it }
				?.let { addCorridorPieces(it, PIECES_CORRIDORS(rand, distanceToPortal), pathLength) }
				?.let { if (lastPieceIsDoor) addDoorPiece(it) else it }
				?.let { addPiece(it, generatedPiece, if (lastPieceIsDoor) MERGE else APPEND) } != null
		}
		
		fun placeRelicRoom(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdRoom_Relic, pathLength: Int): Boolean {
			return targetPiece
				 .let { addDoorPiece(it, MERGE) }
				?.let { addCorridorPieces(it, PIECES_CORRIDOR_RELIC.mutableCopy(), pathLength) }
				?.let { addDoorPiece(it) }
				?.let { addPiece(it, generatedPiece, MERGE) } != null
		}
		
		fun placeDeadEnd(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, pathLength: Int): Boolean {
			return targetPiece
				 .let { addCorridorPieces(it, PIECES_CORRIDOR_DEAD_END.mutableCopy(), pathLength) }
				?.let { addPiece(it, generatedPiece) } != null
		}
		
		// Helpers
		
		private fun addPiece(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, mode: AddMode = APPEND): PositionedPiece<StrongholdInst>? {
			val targetInstance = targetPiece.instance
			val targetConnection = rand.nextItemOrNull(targetInstance.findAvailableConnections()) ?: return null
			
			val facingFromPortal = targetInstance.facingFromPortal ?: targetConnection.facing
			val distanceToPortal = targetInstance.distanceToPortal + (if (generatedPiece.type.shouldIncreaseDistanceToPortal(targetInstance.type)) 1 else 0)
			
			return baseAddPiece(mode, targetPiece, targetConnection) { rotation -> generatedPiece.StrongholdInst(distanceToPortal, facingFromPortal, rotation) }
		}
		
		private fun addDoorPiece(targetPiece: PositionedPiece<StrongholdInst>, mode: AddMode = APPEND): PositionedPiece<StrongholdInst>? {
			return addPiece(targetPiece, PIECES_DOORS.generateItem(rand), mode)
		}
		
		private fun addCorridorPieces(targetPiece: PositionedPiece<StrongholdInst>, corridorPieces: MutableWeightedList<StrongholdAbstractPiece>, corridorLength: Int): PositionedPiece<StrongholdInst>? {
			var lastPiece = targetPiece
			
			repeat(corridorLength) {
				lastPiece = corridorPieces.removeItem(rand)?.let { addPiece(lastPiece, it) } ?: return null
			}
			
			return lastPiece
		}
	}
}

