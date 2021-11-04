package chylex.hee.game.world.generation.feature.tombdungeon

import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonBuilder.TombDungeonBuild
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces.STRUCTURE_SIZE
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.SECRET_CONNECTOR
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_OUTSIDE
import chylex.hee.game.world.generation.feature.tombdungeon.piece.ITombDungeonPieceWithTombs
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonAbstractPiece
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonCorridor_Straight
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonStart
import chylex.hee.game.world.generation.structure.piece.IStructureBuild
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder.ProcessBase
import chylex.hee.game.world.generation.structure.piece.StructureBuild
import chylex.hee.game.world.generation.structure.piece.StructureBuild.AddMode
import chylex.hee.game.world.generation.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.generation.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.generation.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.Rotation4
import chylex.hee.game.world.util.Transform
import chylex.hee.util.collection.WeightedList
import chylex.hee.util.collection.mutableWeightedListOf
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MAX
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import chylex.hee.util.random.removeItem
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.math.BlockPos
import org.apache.commons.lang3.mutable.MutableInt
import java.util.Random
import kotlin.math.min

object TombDungeonBuilder : IStructureBuilder<TombDungeonBuild> {
	val ENTRANCE_POS: BlockPos = STRUCTURE_SIZE.getPos(CENTER, MAX, MAX).add(-TombDungeonStart.size.centerX, -TombDungeonStart.size.y, -STRUCTURE_SIZE.x / 3)
	
	class TombDungeonBuild(val bedrockHeights: List<Int>, delegate: IStructureBuild) : IStructureBuild by delegate
	
	override fun build(rand: Random): TombDungeonBuild? {
		val startingPiece = TombDungeonStart.MutableInstance(Transform.NONE)
		val startingPiecePos = ENTRANCE_POS
		
		val build = StructureBuild(STRUCTURE_SIZE, PositionedPiece(startingPiece, startingPiecePos))
		val process = Process(build, rand)
		
		// entrance stairs
		
		run {
			val stairLengths = mutableListOf<Int>().apply {
				add(rand.nextInt(4, 7))
				add(rand.nextInt(6, 13))
				add(rand.nextInt(27, 28) - sum())
				shuffle(rand)
			}
			
			for ((index, stairLength) in stairLengths.withIndex()) {
				process.appendPieces(build.lastPiece, TombDungeonPieces.PIECES_STAIRCASE(stairLength), level = null) ?: return null
				
				val corridorLength = if (index == stairLengths.lastIndex) rand.nextInt(11, 13) else rand.nextInt(2, rand.nextInt(4, 5))
				val corridorPiece = TombDungeonCorridor_Straight(corridorLength, isFancy = false)
				
				process.addPiece(build.lastPiece, corridorPiece, level = null) ?: return null
			}
		}
		
		// levels
		
		run {
			val cornerCounts = mutableListOf(
				rand.nextInt(3, 4),
				rand.nextInt(3, 5),
				rand.nextInt(4, 5),
				rand.nextInt(5, 6)
			).apply {
				shuffle(rand)
				
				for (level in indices) {
					this[level] += (level - 1) * 2
				}
				
				add(rand.nextInt(12, 13))
			}
			
			val crumblingCorridorCounts = mutableListOf<Pair<Int, MutableInt>>().apply {
				var remaining = rand.nextInt(rand.nextInt(2, 3), 4)
				
				repeat(TombDungeonLevel.values().size - 2) {
					val mainPath = rand.nextInt(0, remaining.coerceIn(0, 1)).also { remaining -= it }
					val sidePath = rand.nextInt(0, remaining.coerceIn(0, 2)).also { remaining -= it }
					add(mainPath to MutableInt(sidePath))
				}
				
				if (remaining > 0) {
					set(0, 0 to MutableInt(remaining))
				}
				
				shuffle(rand)
				add(TombDungeonLevel.FIRST.ordinal, 1 to MutableInt(0)) // have 1 small crumbling corridor on the first floor main path
				add(TombDungeonLevel.LAST.ordinal, 0 to MutableInt(0))
			}
			
			var nextStartPiece = build.lastPiece
			
			for (level in TombDungeonLevel.values()) {
				val crumblingCorridors = crumblingCorridorCounts[level.ordinal]
				val mainPath = TombDungeonPieces.PIECES_MAIN_CORRIDOR(rand, level, cornerCounts[level.ordinal], crumblingCorridors.first)
				
				val mainRooms = process.pickMainRooms(level)
				val sideRooms = process.pickSideRooms(level)
				val sideRoomsMainPath = if (sideRooms.isEmpty() || rand.nextBoolean()) 0 else rand.nextInt(0, sideRooms.size)
				
				val mainPathRooms = mainRooms + (if (sideRoomsMainPath == 0) emptyArray() else sideRooms.sliceArray(0 until sideRoomsMainPath))
				val mainPathRoomSpread = (mainPath.size + mainPathRooms.size) / (mainPathRooms.size + 1)
				
				val mainPathRoomReverseOffset = rand.nextInt(0, mainPathRoomSpread - 1)
				val mainPathRoomMaxForwardOffset = mainPathRoomSpread / 2
				
				for ((index, room) in mainPathRooms.withIndex()) {
					mainPath.add(((index + 1) * mainPathRoomSpread) - mainPathRoomReverseOffset + rand.nextInt(0, mainPathRoomMaxForwardOffset), room)
				}
				
				val firstCorridorIndex = build.generatedPieces.lastIndex + 1
				val lastCorridorIndex = firstCorridorIndex + mainPath.size - 1
				
				if (!build.guardChain(25) {
					process.appendPieces(nextStartPiece, mainPath, level) ?: return@guardChain false
					
					if (!process.generateTombs(firstCorridorIndex, lastCorridorIndex, level.getTombCount(rand), level)) {
						return@guardChain false
					}
					
					if (level == TombDungeonLevel.LAST) {
						process.addPiece(build.generatedPieces[lastCorridorIndex], TombDungeonPieces.PIECE_ROOM_END, level) ?: return@guardChain false
					}
					else {
						process.appendPieces(build.generatedPieces[lastCorridorIndex], TombDungeonPieces.PIECES_STAIRCASE(rand.nextInt(9, 17)), level) ?: return@guardChain false
					}
					
					return@guardChain true
				}) {
					return null
				}
				
				nextStartPiece = build.lastPiece
				
				var sidePathsRemaining = level.getSidePathCount(rand)
				var sideRoomsSidePathIndex = sideRoomsMainPath
				
				outer@ for (sidePathGenAttempt in 0 until sidePathsRemaining * 10) {
					val sideCrumblingCorridors = crumblingCorridors.second.value.coerceAtMost(rand.nextInt(1, 2))
					val sidePath = TombDungeonPieces.PIECES_SIDE_CORRIDOR(rand, level, sideCrumblingCorridors, sideRooms.getOrNull(sideRoomsSidePathIndex))
					
					val targetPieces = build
						.generatedPieces
						.drop(firstCorridorIndex - 1)
						.filter { it.instance.hasAvailableConnections }
						.mapNotNull { (it.instance.owner as? TombDungeonAbstractPiece)?.sidePathAttachWeight?.takeIf { weight -> weight > 0 }?.let { weight -> weight to it } }
						.takeIf { it.isNotEmpty() }
						?.let(::WeightedList)
					
					if (targetPieces == null) {
						break@outer
					}
					
					val firstSideIndex = build.generatedPieces.lastIndex + 1
					val lastSideIndex = firstSideIndex + sidePath.size - 1
					
					for (sidePathAttachAttempt in 1..20) {
						val targetPiece = targetPieces.generateItem(rand)
						
						if (build.guardChain { process.appendPieces(targetPiece, sidePath, level) != null }) {
							++sideRoomsSidePathIndex
							
							for (index in firstSideIndex..lastSideIndex) {
								val piece = build.generatedPieces[index]
								
								if (piece.instance.owner is ITombDungeonPieceWithTombs) {
									process.generateTombs(index, index, rand.nextInt(1, 3), level)
								}
							}
							
							if (sideCrumblingCorridors > 0) {
								crumblingCorridors.second.subtract(sideCrumblingCorridors)
							}
							
							if (--sidePathsRemaining <= 0) {
								break@outer
							}
							else {
								break
							}
						}
					}
				}
				
				if (sidePathsRemaining > 0) {
					return null
				}
			}
		}
		
		if (!process.generateSecrets(6)) {
			return null
		}
		
		return TombDungeonBuild(emptyList(), build.freeze()) // TODO where the fuck did the code go
	}
	
	private class Process(build: StructureBuild<StructurePiece<TombDungeonLevel>.MutableInstance>, rand: Random) : ProcessBase<StructurePiece<TombDungeonLevel>.MutableInstance>(build, rand) {
		private val roomsMain = mapOf(
			false to TombDungeonPieces.PIECES_MAIN_ROOMS_NONFANCY.toMutableList(),
			true to TombDungeonPieces.PIECES_MAIN_ROOMS_FANCY.toMutableList()
		)
		
		private val roomsSide = mapOf(
			false to (TombDungeonPieces.PIECES_SIDE_ROOMS_NONFANCY + TombDungeonPieces.PIECES_SIDE_ROOMS_NONFANCY).toMutableList(),
			true to TombDungeonPieces.PIECES_SIDE_ROOMS_FANCY.toMutableList()
		)
		
		// Room picking
		
		private fun pickRooms(roomList: MutableList<out TombDungeonAbstractPiece>, amount: Int): Array<TombDungeonAbstractPiece> {
			return if (amount == 0)
				emptyArray()
			else
				Array(min(amount, roomList.size)) { rand.removeItem(roomList) }
		}
		
		fun pickMainRooms(level: TombDungeonLevel) = pickRooms(roomsMain.getValue(level.isFancy), rand.nextInt(level.mainRooms))
		fun pickSideRooms(level: TombDungeonLevel) = pickRooms(roomsSide.getValue(level.isFancy), rand.nextInt(level.sideRooms))
		
		// Tomb generation
		
		fun generateTombs(firstIndex: Int, lastIndex: Int, attemptedAmount: Int, level: TombDungeonLevel): Boolean {
			val straightTombs = build
				.generatedPieces
				.subList(firstIndex, lastIndex + 1)
				.filter { piece -> piece.instance.owner is ITombDungeonPieceWithTombs && piece.instance.findAvailableConnections().any { it.type === TOMB_ENTRANCE_OUTSIDE } }
				.associateWith { it.instance.owner as ITombDungeonPieceWithTombs }
				.toList()
			
			if (straightTombs.isEmpty()) {
				return false
			}
			
			var totalGenerated = 0
			
			for (attempt in 0 until attemptedAmount * 4) {
				val (corridor, constructor) = rand.nextItem(straightTombs)
				
				if (addPiece(corridor, constructor.constructTomb(), level, MERGE) != null && ++totalGenerated >= attemptedAmount) {
					break
				}
			}
			
			return totalGenerated >= attemptedAmount / 2
		}
		
		// Secret generation
		
		fun generateSecrets(amount: Int): Boolean {
			val remainingSecrets = TombDungeonPieces.PIECES_SECRET(rand, amount)
			val attachableRooms = mutableWeightedListOf<PositionedPiece<StructurePiece<TombDungeonLevel>.MutableInstance>>()
			
			build.generatedPieces
				.filter { it.instance.owner is TombDungeonAbstractPiece }
				.associateWith { it.instance.owner as TombDungeonAbstractPiece }
				.filterValues { it.secretAttachWeight > 0 }
				.forEach { attachableRooms.addItem(it.value.secretAttachWeight, it.key) }
			
			for (attempt in 1..(amount * 50)) {
				val piece = attachableRooms.removeItem(rand) ?: return false
				val instance = piece.instance
				
				val pieceSize = instance.owner.size
				val pieceTransform = instance.transform
				val secret = remainingSecrets[0]
				val secretMirror = rand.nextBoolean()
				
				val diffX = pieceSize.x - 5
				val diffZ = pieceSize.z - 5
				
				if (diffX < 0 || diffZ < 0) {
					continue
				}
				
				val attachOffset = 2
				val attachY = (instance.owner as TombDungeonAbstractPiece).secretAttachY
				
				for (placementAttempt in 1..7) {
					val side = rand.nextItem(Facing4)
					val attachX: Int
					val attachZ: Int
					
					when (side) {
						NORTH -> { attachX = attachOffset + rand.nextInt(0, diffX); attachZ = 0 }
						SOUTH -> { attachX = attachOffset + rand.nextInt(0, diffX); attachZ = pieceSize.maxZ }
						EAST ->  { attachZ = attachOffset + rand.nextInt(0, diffZ); attachX = pieceSize.maxX }
						else ->  { attachZ = attachOffset + rand.nextInt(0, diffZ); attachX = 0 }
					}
					
					val alignConn = TombDungeonConnection(SECRET_CONNECTOR, pieceTransform(BlockPos(attachX, attachY, attachZ), pieceSize), pieceTransform(side))
					val rotation = Rotation4.first { Transform(it, secretMirror)(secret.entranceFacing) === alignConn.facing.opposite }
					
					val secretInst = secret.MutableInstance(instance.context, Transform(rotation, secretMirror))
					val secretConn = secretInst.findAvailableConnections().single()
					
					val alignOffset = build.alignConnections(secretConn, alignConn, MERGE)
					
					if (build.addPiece(secretInst, piece.offset.add(alignOffset), piece) != null) {
						remainingSecrets.remove(secret)
						break
					}
				}
				
				if (remainingSecrets.isEmpty()) {
					return true
				}
			}
			
			return false
		}
		
		// Helpers
		
		fun appendPieces(targetPiece: PositionedPiece<StructurePiece<TombDungeonLevel>.MutableInstance>, generatedPieces: List<TombDungeonAbstractPiece>, level: TombDungeonLevel?): PositionedPiece<StructurePiece<TombDungeonLevel>.MutableInstance>? {
			return generatedPieces.fold(targetPiece) { lastPiece, nextPiece -> addPiece(lastPiece, nextPiece, level) ?: return null }
		}
		
		fun addPiece(targetPiece: PositionedPiece<StructurePiece<TombDungeonLevel>.MutableInstance>, generatedPiece: TombDungeonAbstractPiece, level: TombDungeonLevel?, mode: AddMode = APPEND): PositionedPiece<StructurePiece<TombDungeonLevel>.MutableInstance>? {
			for (targetConnection in targetPiece.instance.findAvailableConnections().shuffled(rand)) {
				val piece = baseAddPiece(mode, targetPiece, targetConnection) { generatedPiece.MutableInstance(level, it) }
				
				if (piece != null) {
					return piece
				}
			}
			
			return null
		}
	}
}
