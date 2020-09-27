package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class StrongholdRoom_Decor_TwoFloorIntersection(file: String) : StrongholdRoom_Decor_Generic(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 0, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 5, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(centerX, 5, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(maxX, 5, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, centerZ), WEST),
		StrongholdConnection(ROOM, Pos(0, 5, centerZ), WEST)
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
		world.placeCube(Pos(centerX, y, 1), Pos(centerX, y, maxZ - 1), Single(Blocks.CHISELED_STONE_BRICKS))
		world.placeCube(Pos(1, y, centerZ), Pos(maxX - 1, y, centerZ), Single(Blocks.CHISELED_STONE_BRICKS))
	}
}
