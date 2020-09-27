package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.with
import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextItemOrNull
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

open class TombDungeonRoom_Side_Crossroads(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, centerZ), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val reverseTransform = instance.transform.reverse
		val availableChestSides = mutableListOf<Pair<BlockPos, Direction>>()
		
		for(connection in instance.findAvailableConnections()){
			val pos = reverseTransform(connection.offset, instance.size).up(2)
			val facing = reverseTransform(connection.facing)
			val opposite = facing.opposite
			
			world.setBlock(pos, ModBlocks.DUSTY_STONE_DECORATION)
			
			world.setState(pos.offset(opposite, 1).offset(facing.rotateY(), 2), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(facing))
			world.setState(pos.offset(opposite, 1).offset(facing.rotateYCCW(), 2), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(facing))
			
			world.setState(pos.offset(opposite, 2).offset(facing.rotateY(), 2), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(opposite))
			world.setState(pos.offset(opposite, 2).offset(facing.rotateYCCW(), 2), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(opposite))
			
			availableChestSides.add(pos to facing)
		}
		
		rand.nextItemOrNull(availableChestSides)?.let { (pos, facing) ->
			val opposite = facing.opposite
			val center = pos.offset(opposite)
			
			val posAbove = center.up()
			val posBelow = center.down()
			
			world.setState(posAbove, ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP))
			world.setState(posAbove.offset(facing.rotateY()), ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP))
			world.setState(posAbove.offset(facing.rotateYCCW()), ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP))
			
			world.setState(posBelow, ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(facing).with(BlockStairs.HALF, Half.TOP))
			world.setState(posBelow.offset(facing.rotateY()), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(facing).with(BlockStairs.HALF, Half.TOP))
			world.setState(posBelow.offset(facing.rotateYCCW()), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(facing).with(BlockStairs.HALF, Half.TOP))
			
			world.setBlock(center.offset(facing.rotateY(), 2), ModBlocks.DUSTY_STONE_BRICKS)
			world.setBlock(center.offset(facing.rotateYCCW(), 2), ModBlocks.DUSTY_STONE_BRICKS)
			
			world.setBlock(center.offset(opposite).offset(facing.rotateY(), 2), ModBlocks.DUSTY_STONE_BRICKS)
			world.setBlock(center.offset(opposite).offset(facing.rotateYCCW(), 2), ModBlocks.DUSTY_STONE_BRICKS)
			
			placeChest(world, instance, center, opposite)
		}
	}
}
