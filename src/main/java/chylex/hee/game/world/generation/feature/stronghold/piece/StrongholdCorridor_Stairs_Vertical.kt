package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.SLAB_TYPE
import chylex.hee.game.block.util.with
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.STAIR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.MutablePos
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.block.Blocks
import net.minecraft.state.properties.SlabType
import net.minecraft.util.Direction

class StrongholdCorridor_Stairs_Vertical(connectionAtEntrance: Direction, connectionAtExit: Direction, levels: Int) : StrongholdAbstractPiece() {
	private companion object {
		/*
		 * base height for levels = 1 is 9
		 * if exit is to the left of entrance, add 0
		 * if exit is the same as entrance (straight), add 1
		 * if exit is to the right of entrance, add 2
		 * if exit is opposite of entrance (return), add 3
		 * base height for levels = 2 is 13
		 */
		private fun calculateHeight(connectionAtEntrance: Direction, connectionAtExit: Direction, levels: Int): Int {
			val revEntrance = connectionAtEntrance.opposite
			
			return 9 + ((levels - 1) * 4) + when (connectionAtExit) {
				revEntrance.opposite  -> 3
				revEntrance.rotateY() -> 2
				revEntrance           -> 1
				else                  -> 0
			}
		}
	}
	
	override val type = StrongholdPieceType.CORRIDOR
	override val size = Size(5, calculateHeight(connectionAtEntrance, connectionAtExit, levels), 5)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(STAIR, Pos(size.centerX, 0, size.centerZ).offset(connectionAtEntrance, 2), connectionAtEntrance),
		StrongholdConnection(STAIR, Pos(size.centerX, size.maxY - 4, size.centerZ).offset(connectionAtExit, 2), connectionAtExit)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
		
		val rand = world.rand
		val useStairs = rand.nextBoolean()
		
		var facing = connections[0].facing.opposite
		val pos = MutablePos(connections[0].offset).move(facing).move(facing.rotateYCCW())
		
		repeat(size.maxY - 4) {
			++pos.y
			
			val firstBlock = if (useStairs)
				Blocks.STONE_BRICK_STAIRS.withFacing(facing)
			else
				Blocks.STONE_BRICK_SLAB.with(SLAB_TYPE, SlabType.BOTTOM)
			
			pos.move(facing)
			world.setState(pos, firstBlock)
			pos.move(facing)
			world.setState(pos, Blocks.STONE_BRICK_SLAB.with(SLAB_TYPE, SlabType.TOP))
			
			facing = facing.rotateY()
		}
		
		if (rand.nextBoolean()) {
			world.placeCube(Pos(size.centerX, 1, size.centerZ), Pos(size.centerX, size.maxY - 1, size.centerZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
			
			val endFacing = connections[1].facing.opposite
			val endOffset = connections[1].offset.up().offset(endFacing, 1)
			
			world.placeCube(endOffset, endOffset.offset(endFacing.rotateYCCW(), 1).up(2), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		}
	}
}
