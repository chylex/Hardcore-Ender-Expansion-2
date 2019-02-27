package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class StrongholdRoom_Decor_TwoFloorIntersection(file: String) : StrongholdRoom_Decor_Generic(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 5, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(centerX, 5, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(maxX, 5, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST),
		StrongholdRoomConnection(Pos(0, 5, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextInt(5) <= 1){
			placeChiseledCross(world, 0)
		}
		
		if (rand.nextInt(5) <= 1){
			placeChiseledCross(world, 5)
		}
	}
	
	private fun placeChiseledCross(world: IStructureWorld, y: Int){
		world.placeCube(Pos(centerX, y, 1), Pos(centerX, y, maxZ - 1), Single(FutureBlocks.CHISELED_STONE_BRICKS))
		world.placeCube(Pos(1, y, centerZ), Pos(maxX - 1, y, centerZ), Single(FutureBlocks.CHISELED_STONE_BRICKS))
	}
}
