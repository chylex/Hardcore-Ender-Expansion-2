package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class StrongholdCorridor_Intersection private constructor(vararg connections: Direction) : StrongholdAbstractPiece() {
	companion object {
		val CORNER   = StrongholdCorridor_Intersection(SOUTH, WEST)
		val THREEWAY = StrongholdCorridor_Intersection(SOUTH, WEST, EAST)
		val FOURWAY  = StrongholdCorridor_Intersection(SOUTH, WEST, EAST, NORTH)
	}
	
	override val type = StrongholdPieceType.CORRIDOR
	override val size = Size(5, 5, 5)
	
	override val connections = connections.map {
		when (it) {
			NORTH -> StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH)
			SOUTH -> StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH)
			EAST  -> StrongholdConnection(CORRIDOR, Pos(size.maxX, 0, size.centerZ), EAST)
			WEST  -> StrongholdConnection(CORRIDOR, Pos(0, 0, size.centerZ), WEST)
			else  -> throw IllegalArgumentException()
		}
	}.toTypedArray<IStructurePieceConnection>()
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
	}
}
