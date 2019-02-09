package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdCorridorConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos.MutableBlockPos

class StrongholdCorridor_Stairs_Vertical(connectionAtEntrance: EnumFacing, connectionAtExit: EnumFacing, levels: Int) : StrongholdAbstractPiece(){
	private companion object{
		/*
		 * base height for levels = 1 is 9
		 * if exit is to the left of entrance, add 0
		 * if exit is the same as entrance (straight), add 1
		 * if exit is to the right of entrance, add 2
		 * if exit is opposite of entrance (return), add 3
		 * base height for levels = 2 is 13
		 */
		private fun calculateHeight(connectionAtEntrance: EnumFacing, connectionAtExit: EnumFacing, levels: Int): Int{
			val revEntrance = connectionAtEntrance.opposite
			
			return 9 + ((levels - 1) * 4) + when(connectionAtExit){
				revEntrance.opposite -> 3
				revEntrance.rotateY() -> 2
				revEntrance -> 1
				else -> 0
			}
		}
	}
	
	override val size = Size(5, calculateHeight(connectionAtEntrance, connectionAtExit, levels), 5)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdCorridorConnection(Pos(size.centerX, 0, size.centerZ).offset(connectionAtEntrance, 2), connectionAtEntrance),
		StrongholdCorridorConnection(Pos(size.centerX, size.maxY - 4, size.centerZ).offset(connectionAtExit, 2), connectionAtExit)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Single(Blocks.AIR))
		
		val rand = world.rand
		val useStairs = rand.nextBoolean()
		
		var facing = connections[0].facing.opposite
		val pos = MutableBlockPos(connections[0].offset).move(facing).move(facing.rotateYCCW())
		
		repeat(size.maxY - 4){
			++pos.y
			
			val firstBlock = if (useStairs)
				Blocks.STONE_BRICK_STAIRS.defaultState.withProperty(BlockStairs.FACING, facing)
			else
				FutureBlocks.STONE_BRICK_SLAB.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM)
			
			pos.move(facing)
			world.setState(pos, firstBlock)
			pos.move(facing)
			world.setState(pos, FutureBlocks.STONE_BRICK_SLAB.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP))
			
			facing = facing.rotateY()
		}
		
		if (rand.nextBoolean()){
			world.placeCube(Pos(size.centerX, 1, size.centerZ), Pos(size.centerX, size.maxY - 1, size.centerZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
			
			val endFacing = connections[1].facing.opposite
			val endOffset = connections[1].offset.up().offset(endFacing, 1)
			
			world.placeCube(endOffset, endOffset.offset(endFacing.rotateYCCW(), 1).up(2), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		}
	}
}
