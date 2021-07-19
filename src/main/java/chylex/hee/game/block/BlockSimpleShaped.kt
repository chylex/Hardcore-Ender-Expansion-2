package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.util.asVoxelShape
import net.minecraft.block.BlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.world.IBlockReader

open class BlockSimpleShaped(builder: BlockBuilder, aabb: AxisAlignedBB) : HeeBlock(builder) {
	private val shape = aabb.asVoxelShape
	override fun getShape(state: BlockState, source: IBlockReader, pos: BlockPos, context: ISelectionContext) = shape
}
