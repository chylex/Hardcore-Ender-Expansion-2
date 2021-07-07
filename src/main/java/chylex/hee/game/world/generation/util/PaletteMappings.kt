package chylex.hee.game.world.generation.util

import chylex.hee.game.block.util.FOUR_WAY_EAST
import chylex.hee.game.block.util.FOUR_WAY_NORTH
import chylex.hee.game.block.util.FOUR_WAY_SOUTH
import chylex.hee.game.block.util.FOUR_WAY_WEST
import chylex.hee.game.block.util.with
import net.minecraft.block.Block
import net.minecraft.block.DirectionalBlock
import net.minecraft.block.HorizontalBlock
import net.minecraft.block.VineBlock
import net.minecraft.block.WallBlock
import net.minecraft.block.WallHeight
import net.minecraft.state.properties.DoubleBlockHalf
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType
import net.minecraft.state.properties.StairsShape
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST

object PaletteMappings {
	val FACING_ALL = DirectionalBlock.FACING to mapOf(
		"up"    to UP,
		"down"  to DOWN,
		"north" to NORTH,
		"south" to SOUTH,
		"east"  to EAST,
		"west"  to WEST,
	)
	
	val FACING_HORIZONTAL = HorizontalBlock.HORIZONTAL_FACING to mapOf(
		"north" to NORTH,
		"south" to SOUTH,
		"east"  to EAST,
		"west"  to WEST,
	)
	
	val FACING_AXIS = chylex.hee.game.block.util.ROTATED_PILLAR_AXIS to mapOf(
		"ew" to Axis.X,
		"ud" to Axis.Y,
		"ns" to Axis.Z,
	)
	
	val DOOR_HALF = chylex.hee.game.block.util.DOOR_HALF to mapOf(
		"bottom" to DoubleBlockHalf.LOWER,
		"top"    to DoubleBlockHalf.UPPER,
	)
	
	val TRAPDOOR_HALF = chylex.hee.game.block.util.TRAPDOOR_HALF to mapOf(
		"bottom" to Half.BOTTOM,
		"top"    to Half.TOP,
	)
	
	val TRAPDOOR_OPEN = chylex.hee.game.block.util.TRAPDOOR_OPEN to mapOf(
		""     to false,
		"open" to true,
	)
	
	val SLAB_TYPE = chylex.hee.game.block.util.SLAB_TYPE to mapOf(
		"bottom" to SlabType.BOTTOM,
		"top"    to SlabType.TOP,
		"double" to SlabType.DOUBLE,
	)
	
	val STAIR_FLIP = chylex.hee.game.block.util.STAIRS_HALF to mapOf(
		""     to Half.BOTTOM,
		"flip" to Half.TOP,
	)
	
	val STAIR_SHAPE = chylex.hee.game.block.util.STAIRS_SHAPE to mapOf(
		""   to StairsShape.STRAIGHT,
		"il" to StairsShape.INNER_LEFT,
		"ir" to StairsShape.INNER_RIGHT,
		"ol" to StairsShape.OUTER_LEFT,
		"or" to StairsShape.OUTER_RIGHT,
	)
	
	val TRAPDOOR_MAPPING_LIST = listOf(TRAPDOOR_HALF, TRAPDOOR_OPEN, FACING_HORIZONTAL)
	val STAIR_MAPPING_LIST = listOf(STAIR_FLIP, FACING_HORIZONTAL, STAIR_SHAPE)
	
	private val LIST_NSEW
		get() = arrayOf("n", "s", "e", "w", "ns", "ne", "nw", "se", "sw", "ew", "nse", "nsw", "new", "sew", "nsew")
	
	private val LIST_UP_NSEW
		get() = arrayOf("u") + LIST_NSEW.map { "u$it" }
	
	fun HORIZONTAL_CONNECTIONS(block: Block) = (arrayOf("") + LIST_NSEW).associate {
		Pair(it, it.fold(block.defaultState) { state, chr ->
			when (chr) {
				'n'  -> state.with(FOUR_WAY_NORTH, true)
				's'  -> state.with(FOUR_WAY_SOUTH, true)
				'e'  -> state.with(FOUR_WAY_EAST, true)
				'w'  -> state.with(FOUR_WAY_WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
	
	fun WALL_CONNECTIONS(block: Block) = (LIST_NSEW + LIST_UP_NSEW).associate {
		Pair(it, it.fold(block.with(WallBlock.UP, false)) { state, chr ->
			when (chr) {
				'u'  -> state.with(WallBlock.UP, true)
				'n'  -> state.with(WallBlock.WALL_HEIGHT_NORTH, WallHeight.LOW)
				's'  -> state.with(WallBlock.WALL_HEIGHT_SOUTH, WallHeight.LOW)
				'e'  -> state.with(WallBlock.WALL_HEIGHT_EAST, WallHeight.LOW)
				'w'  -> state.with(WallBlock.WALL_HEIGHT_WEST, WallHeight.LOW)
				else -> throw IllegalStateException()
			}
		})
	}
	
	fun VINE_WALLS(block: Block) = (arrayOf("u") + LIST_NSEW).associate {
		Pair(it, it.fold(block.defaultState) { state, chr ->
			when (chr) {
				'u'  -> state.with(VineBlock.UP, true)
				'n'  -> state.with(VineBlock.NORTH, true)
				's'  -> state.with(VineBlock.SOUTH, true)
				'e'  -> state.with(VineBlock.EAST, true)
				'w'  -> state.with(VineBlock.WEST, true)
				else -> throw IllegalStateException()
			}
		})
	}
}
