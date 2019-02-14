package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdCorridorConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class StrongholdCorridor_Intersection(vararg connections: EnumFacing) : StrongholdAbstractPiece(){
	override val size = Size(5, 5, 5)
	
	override val connections = connections.map {
		when(it){
			NORTH -> StrongholdCorridorConnection(Pos(size.centerX, 0, 0), NORTH)
			SOUTH -> StrongholdCorridorConnection(Pos(size.centerX, 0, size.maxZ), SOUTH)
			EAST -> StrongholdCorridorConnection(Pos(size.maxX, 0, size.centerZ), EAST)
			WEST -> StrongholdCorridorConnection(Pos(0, 0, size.centerZ), WEST)
			else -> throw IllegalArgumentException()
		}
	}.toTypedArray<IStructurePieceConnection>()
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Single(Blocks.AIR))
	}
}