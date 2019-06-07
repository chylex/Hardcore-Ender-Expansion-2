package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdCorridor_Straight(length: Int) : StrongholdAbstractPiece(){
	override val type = StrongholdPieceType.CORRIDOR
	override val size = Size(5, 5, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH),
		StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
	}
}
