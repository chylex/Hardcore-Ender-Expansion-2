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
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.piece.IStructureBuilder
import chylex.hee.game.world.structure.piece.StructureBuild
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.util.Rotation4
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.removeItem
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.max
import kotlin.math.min

object StrongholdBuilder : IStructureBuilder{
	fun buildWithEyeOfEnderTarget(rand: Random): Pair<IStructureGenerator, BlockPos?>?{
		val build = StructureBuild(STRUCTURE_SIZE, rand.nextItem(PIECES_START).StrongholdInst(distanceToPortal = 0, facingFromPortal = null, rotation = rand.nextItem()))
		val process = Process(build, rand)
		
		// normal rooms
		
		run {
			val remainingRooms = PIECES_ROOMS(rand)
			val totalAttempts = remainingRooms.size * 5
			
			for(totalAttempt in 1..totalAttempts){
				val nextTargetPiece = process.pickNextTargetPiece() ?: break
				val nextGeneratedPiece = remainingRooms.getOrNull(rand.nextInt(0, min(1, remainingRooms.size - 1))) ?: break
				
				if (build.guardChain(5){ process.placeNormalRoom(nextTargetPiece, nextGeneratedPiece, pathLength = rand.nextInt(1, 8)) }){
					remainingRooms.remove(nextGeneratedPiece)
					
					if (remainingRooms.isEmpty()){
						break
					}
				}
			}
			
			if (remainingRooms.isNotEmpty()){
				HEE.log.debug("[StrongholdBuilder] failed at rooms")
				return null
			}
		}
		
		// relic rooms
		
		run {
			val openPiecesByDistance = build.generatedPieces.filter { it.instance.hasAvailableConnections }.sortedByDescending { it.instance.distanceToPortal }.toList()
			val remainingRelicTargets = openPiecesByDistance.take(max(10, openPiecesByDistance.size / 3)).toMutableList()
			val remainingRelicRooms = PIECES_RELICS(rand)
			
			var lastRelicFacingFromPortal: EnumFacing? = null
			
			while(remainingRelicRooms.isNotEmpty()){
				val targetPiece = rand.removeItem(remainingRelicTargets) ?: break
				val targetInstance = targetPiece.instance
				
				if (targetInstance.facingFromPortal != lastRelicFacingFromPortal){
					val relicRoom = rand.nextItem(remainingRelicRooms)
					
					if (build.guardChain(5){ process.placeRelicRoom(targetPiece, relicRoom, pathLength = rand.nextInt(9, 10)) }){
						remainingRelicRooms.remove(relicRoom)
						lastRelicFacingFromPortal = targetInstance.facingFromPortal
					}
				}
			}
			
			if (remainingRelicRooms.isNotEmpty()){
				HEE.log.debug("[StrongholdBuilder] failed at relics")
				return null
			}
		}
		
		// dead ends
		
		run {
			val openPiecesForDeadEnds = build.generatedPieces.filter { it.instance.canLeadIntoDeadEnd }.shuffled(rand)
			val remainingDeadEnds = PIECES_DEAD_ENDS(rand)
			
			var lastDeadEndFacingFromPortal: EnumFacing? = null
			var lastDeadEndFacingCounter = 0
			
			for(targetPiece in openPiecesForDeadEnds){
				val targetFacing = targetPiece.instance.facingFromPortal
				
				if (targetFacing != lastDeadEndFacingFromPortal || lastDeadEndFacingCounter == 0){
					val deadEnd = rand.nextItem(remainingDeadEnds)
					
					if (build.guardChain(5){ process.placeDeadEnd(targetPiece, deadEnd, pathLength = rand.nextInt(3, 7)) }){
						remainingDeadEnds.remove(deadEnd)
						
						if (remainingDeadEnds.isEmpty()){
							break
						}
						
						if (targetFacing == lastDeadEndFacingFromPortal){
							++lastDeadEndFacingCounter
						}
						else{
							lastDeadEndFacingFromPortal = targetFacing
							lastDeadEndFacingCounter = 0
						}
					}
				}
			}
			
			if (remainingDeadEnds.isNotEmpty()){
				HEE.log.debug("[StrongholdBuilder] failed at dead ends")
				return null
			}
		}
		
		// finish
		
		val generator = build.freeze()
		val eyeOfEnderTargets = build.generatedPieces.filter { it.instance.isEyeOfEnderTarget }.shuffled(rand)
		
		return generator to eyeOfEnderTargets.firstOrNull()?.pieceBox?.center?.subtract(STRUCTURE_SIZE.centerPos)
	}
	
	override fun build(rand: Random): IStructureGenerator?{
		return buildWithEyeOfEnderTarget(rand)?.first
	}
	
	private class Process(private val build: StructureBuild<StrongholdInst>, private val rand: Random){
		
		// Piece picking
		
		fun pickNextTargetPiece(): PositionedPiece<StrongholdInst>?{
			val weightedPieces = build.generatedPieces.mapNotNull {
				val weight = it.instance.pickWeight
				if (weight == 0) null else weight to it
			}
			
			return weightedPieces.ifEmpty { null }?.let(::WeightedList)?.generateItem(rand)
		}
		
		// Piece placement
		
		fun placeNormalRoom(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, pathLength: Int): Boolean{
			val distanceToPortal = targetPiece.instance.distanceToPortal
			val corridorPieces = PIECES_CORRIDORS(rand, distanceToPortal)
			
			val firstPieceIsDoor = distanceToPortal > 0 && rand.nextInt(5) <= 2
			val lastPieceIsDoor = (!firstPieceIsDoor || pathLength > 3) && rand.nextInt(5) <= 2
			
			var lastPiece = targetPiece
			
			if (firstPieceIsDoor){
				lastPiece = addDoorPiece(targetPiece, MERGE) ?: return false
			}
			
			repeat(pathLength){
				lastPiece = corridorPieces.removeItem(rand)?.let { addPiece(lastPiece, it) } ?: return false
			}
			
			if (lastPieceIsDoor){
				lastPiece = addDoorPiece(lastPiece) ?: return false
			}
			
			return addPiece(lastPiece, generatedPiece, if (lastPieceIsDoor) MERGE else APPEND) != null
		}
		
		fun placeRelicRoom(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdRoom_Relic, pathLength: Int): Boolean{
			val corridorPieces = PIECES_CORRIDOR_RELIC.mutableCopy()
			var lastPiece = addDoorPiece(targetPiece, MERGE) ?: return false
			
			repeat(pathLength){
				lastPiece = corridorPieces.removeItem(rand)?.let { addPiece(lastPiece, it) } ?: return false
			}
			
			lastPiece = addDoorPiece(lastPiece) ?: return false
			return addPiece(lastPiece, generatedPiece, MERGE) != null
		}
		
		fun placeDeadEnd(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, pathLength: Int): Boolean{
			val corridorPieces = PIECES_CORRIDOR_DEAD_END.mutableCopy()
			var lastPiece = targetPiece
			
			repeat(pathLength){
				lastPiece = corridorPieces.removeItem(rand)?.let { addPiece(lastPiece, it) } ?: return false
			}
			
			return addPiece(lastPiece, generatedPiece) != null
		}
		
		// Helpers
		
		private fun addPiece(targetPiece: PositionedPiece<StrongholdInst>, generatedPiece: StrongholdAbstractPiece, mode: AddMode = APPEND): PositionedPiece<StrongholdInst>?{
			val targetInstance = targetPiece.instance
			val targetConnection = rand.nextItemOrNull(targetInstance.findValidConnections()) ?: return null
			
			val facingFromPortal = targetInstance.facingFromPortal ?: targetConnection.facing
			val distanceToPortal = targetInstance.distanceToPortal + (if (generatedPiece.type.shouldIncreaseDistanceToPortal(targetInstance.type)) 1 else 0)
			
			for(rotation in Rotation4.randomPermutation(rand)){
				val inst = generatedPiece.StrongholdInst(distanceToPortal, facingFromPortal, rotation)
				val connections = inst.findValidConnections(targetConnection)
				
				if (connections.isNotEmpty()){
					return build.addPiece(inst, rand.nextItem(connections), targetPiece, targetConnection, mode)
				}
			}
			
			return null
		}
		
		private fun addDoorPiece(targetPiece: PositionedPiece<StrongholdInst>, mode: AddMode = APPEND): PositionedPiece<StrongholdInst>?{
			return addPiece(targetPiece, PIECES_DOORS.generateItem(rand), mode)
		}
	}
}

