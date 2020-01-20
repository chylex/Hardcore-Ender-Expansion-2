package chylex.hee.game.world.structure.file
import chylex.hee.system.migration.Facing.AXIS_X
import chylex.hee.system.migration.Facing.AXIS_Y
import chylex.hee.system.migration.Facing.AXIS_Z
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.vanilla.BlockDirectional
import chylex.hee.system.migration.vanilla.BlockDoor
import chylex.hee.system.migration.vanilla.BlockFourWay
import chylex.hee.system.migration.vanilla.BlockHorizontal
import chylex.hee.system.migration.vanilla.BlockRotatedPillar
import chylex.hee.system.migration.vanilla.BlockSlab
import chylex.hee.system.migration.vanilla.BlockStairs
import chylex.hee.system.migration.vanilla.BlockTrapDoor
import chylex.hee.system.migration.vanilla.BlockVine
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.WallBlock
import net.minecraft.state.properties.DoubleBlockHalf
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType
import net.minecraft.state.properties.StairsShape

object PaletteMappings{
	val FACING_ALL = BlockDirectional.FACING to mapOf(
		"up" to UP,
		"down" to DOWN,
		"north" to NORTH,
		"south" to SOUTH,
		"east" to EAST,
		"west" to WEST
	)
	
	val FACING_HORIZONTAL = BlockHorizontal.HORIZONTAL_FACING to mapOf(
		"north" to NORTH,
		"south" to SOUTH,
		"east" to EAST,
		"west" to WEST
	)
	
	val FACING_AXIS = BlockRotatedPillar.AXIS to mapOf(
		"ew" to AXIS_X,
		"ud" to AXIS_Y,
		"ns" to AXIS_Z
	)
	
	val DOOR_HALF = BlockDoor.HALF to mapOf(
		"bottom" to DoubleBlockHalf.LOWER,
		"top" to DoubleBlockHalf.UPPER
	)
	
	val TRAPDOOR_HALF = BlockTrapDoor.HALF to mapOf(
		"bottom" to Half.BOTTOM,
		"top" to Half.TOP
	)
	
	val TRAPDOOR_OPEN = BlockTrapDoor.OPEN to mapOf(
		"" to false,
		"open" to true
	)
	
	val SLAB_TYPE = BlockSlab.TYPE to mapOf(
		"bottom" to SlabType.BOTTOM,
		"top" to SlabType.TOP,
		"double" to SlabType.DOUBLE
	)
	
	val STAIR_FLIP = BlockStairs.HALF to mapOf(
		"" to Half.BOTTOM,
		"flip" to Half.TOP
	)
	
	val STAIR_SHAPE = BlockStairs.SHAPE to mapOf(
		"" to StairsShape.STRAIGHT,
		"il" to StairsShape.INNER_LEFT,
		"ir" to StairsShape.INNER_RIGHT,
		"ol" to StairsShape.OUTER_LEFT,
		"or" to StairsShape.OUTER_RIGHT
	)
	
	val TRAPDOOR_MAPPING_LIST = listOf(TRAPDOOR_HALF, TRAPDOOR_OPEN, FACING_HORIZONTAL)
	val STAIR_MAPPING_LIST = listOf(STAIR_FLIP, FACING_HORIZONTAL, STAIR_SHAPE)
	
	private val LIST_NSEW
		get() = arrayOf("n", "s", "e", "w", "ns", "ne", "nw", "se", "sw", "ew", "nse", "nsw", "new", "sew", "nsew")
	
	private val LIST_UP_NSEW
		get() = arrayOf("u") + LIST_NSEW.map { "u$it" }
	
	fun HORIZONTAL_CONNECTIONS(block: Block) = (arrayOf("") + LIST_NSEW).associate {
		Pair(it, it.fold(block.defaultState){
			state, chr -> when(chr){
				'n' -> state.with(BlockFourWay.NORTH, true)
				's' -> state.with(BlockFourWay.SOUTH, true)
				'e' -> state.with(BlockFourWay.EAST, true)
				'w' -> state.with(BlockFourWay.WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
	
	fun WALL_CONNECTIONS(block: Block) = (LIST_NSEW + LIST_UP_NSEW).associate {
		Pair(it, it.fold(block.with(WallBlock.UP, false)){
			state, chr -> when(chr){
				'u' -> state.with(WallBlock.UP, true)
				'n' -> state.with(WallBlock.NORTH, true)
				's' -> state.with(WallBlock.SOUTH, true)
				'e' -> state.with(WallBlock.EAST, true)
				'w' -> state.with(WallBlock.WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
	
	fun VINE_WALLS(block: Block) = (arrayOf("u") + LIST_NSEW).associate {
		Pair(it, it.fold(block.defaultState){
			state, chr -> when(chr){
				'u' -> state.with(BlockVine.UP, true)
				'n' -> state.with(BlockVine.NORTH, true)
				's' -> state.with(BlockVine.SOUTH, true)
				'e' -> state.with(BlockVine.EAST, true)
				'w' -> state.with(BlockVine.WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
}
