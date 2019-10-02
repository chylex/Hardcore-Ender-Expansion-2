package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing

class StrongholdCorridor_Intersection private constructor(vararg connections: EnumFacing) : StrongholdAbstractPiece(){
	companion object{
		val CORNER   = StrongholdCorridor_Intersection(SOUTH, WEST)
		val THREEWAY = StrongholdCorridor_Intersection(SOUTH, WEST, EAST)
		val FOURWAY  = StrongholdCorridor_Intersection(SOUTH, WEST, EAST, NORTH)
	}
	
	override val type = StrongholdPieceType.CORRIDOR
	override val size = Size(5, 5, 5)
	
	override val connections = connections.map {
		when(it){
			NORTH -> StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH)
			SOUTH -> StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH)
			EAST -> StrongholdConnection(CORRIDOR, Pos(size.maxX, 0, size.centerZ), EAST)
			WEST -> StrongholdConnection(CORRIDOR, Pos(0, 0, size.centerZ), WEST)
			else -> throw IllegalArgumentException()
		}
	}.toTypedArray<IStructurePieceConnection>()
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
	}
}
