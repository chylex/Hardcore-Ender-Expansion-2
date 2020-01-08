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
import chylex.hee.system.migration.vanilla.BlockHorizontal
import chylex.hee.system.migration.vanilla.BlockRotatedPillar
import chylex.hee.system.migration.vanilla.BlockSlab
import chylex.hee.system.migration.vanilla.BlockStairs
import chylex.hee.system.migration.vanilla.BlockTorchWall
import chylex.hee.system.migration.vanilla.BlockTrapDoor
import chylex.hee.system.migration.vanilla.BlockVine
import net.minecraft.state.properties.DoubleBlockHalf
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType

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
	
	val FACING_WALL_TORCH = BlockTorchWall.HORIZONTAL_FACING to mapOf(
		"" to UP,
		"north" to NORTH,
		"south" to SOUTH,
		"east" to EAST,
		"west" to WEST
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
	
	val TRAPDOOR_MAPPING_LIST = listOf(TRAPDOOR_HALF, TRAPDOOR_OPEN, FACING_HORIZONTAL)
	val STAIR_MAPPING_LIST = listOf(STAIR_FLIP, FACING_HORIZONTAL)
	
	fun VINE_WALLS(block: BlockVine) = arrayOf("u", "n", "s", "e", "w", "ns", "ne", "nw", "se", "sw", "ew", "nse", "nsw", "new", "sew", "nsew").associate {
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
