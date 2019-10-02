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
import chylex.hee.system.util.with
import net.minecraft.block.BlockDirectional
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.BlockLog
import net.minecraft.block.BlockRotatedPillar
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.block.BlockTorch
import net.minecraft.block.BlockTrapDoor
import net.minecraft.block.BlockVine

object PaletteMappings{
	val FACING_ALL = BlockDirectional.FACING to mapOf(
		"up" to UP,
		"down" to DOWN,
		"north" to NORTH,
		"south" to SOUTH,
		"east" to EAST,
		"west" to WEST
	)
	
	val FACING_HORIZONTAL = BlockHorizontal.FACING to mapOf(
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
	
	val FACING_AXIS_LOGS = BlockLog.LOG_AXIS to mapOf(
		"ew" to BlockLog.EnumAxis.X,
		"ud" to BlockLog.EnumAxis.Y,
		"ns" to BlockLog.EnumAxis.Z
	)
	
	val FACING_TORCH = BlockTorch.FACING to mapOf(
		"" to UP,
		"north" to NORTH,
		"south" to SOUTH,
		"east" to EAST,
		"west" to WEST
	)
	
	val DOOR_HALF = BlockDoor.HALF to mapOf(
		"bottom" to BlockDoor.EnumDoorHalf.LOWER,
		"top" to BlockDoor.EnumDoorHalf.UPPER
	)
	
	val TRAPDOOR_HALF = BlockTrapDoor.HALF to mapOf(
		"bottom" to BlockTrapDoor.DoorHalf.BOTTOM,
		"top" to BlockTrapDoor.DoorHalf.TOP
	)
	
	val TRAPDOOR_OPEN = BlockTrapDoor.OPEN to mapOf(
		"" to false,
		"open" to true
	)
	
	val SLAB_HALF = BlockSlab.HALF to mapOf(
		"bottom" to BlockSlab.EnumBlockHalf.BOTTOM,
		"top" to BlockSlab.EnumBlockHalf.TOP
	)
	
	val STAIR_FLIP = BlockStairs.HALF to mapOf(
		"" to BlockStairs.EnumHalf.BOTTOM,
		"flip" to BlockStairs.EnumHalf.TOP
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
