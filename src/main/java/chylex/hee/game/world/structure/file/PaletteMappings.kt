package chylex.hee.game.world.structure.file
import net.minecraft.block.BlockDirectional
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.BlockLog
import net.minecraft.block.BlockRotatedPillar
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.block.BlockTorch
import net.minecraft.block.BlockVine
import net.minecraft.util.EnumFacing.Axis.X
import net.minecraft.util.EnumFacing.Axis.Y
import net.minecraft.util.EnumFacing.Axis.Z
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST

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
		"ew" to X,
		"ud" to Y,
		"ns" to Z
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
	
	val SLAB_HALF = BlockSlab.HALF to mapOf(
		"bottom" to BlockSlab.EnumBlockHalf.BOTTOM,
		"top" to BlockSlab.EnumBlockHalf.TOP
	)
	
	val STAIR_FLIP = BlockStairs.HALF to mapOf(
		"" to BlockStairs.EnumHalf.BOTTOM,
		"flip" to BlockStairs.EnumHalf.TOP
	)
	
	fun VINE_WALLS(block: BlockVine) = arrayOf("u", "n", "s", "e", "w", "ns", "ne", "nw", "se", "sw", "ew", "nse", "nsw", "new", "sew", "nsew").associate {
		Pair(it, it.fold(block.defaultState){
			state, chr -> when(chr){
				'u' -> state.withProperty(BlockVine.UP, true)
				'n' -> state.withProperty(BlockVine.NORTH, true)
				's' -> state.withProperty(BlockVine.SOUTH, true)
				'e' -> state.withProperty(BlockVine.EAST, true)
				'w' -> state.withProperty(BlockVine.WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
}
