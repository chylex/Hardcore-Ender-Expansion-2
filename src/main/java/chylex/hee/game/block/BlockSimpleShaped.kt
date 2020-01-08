package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.util.asVoxelShape
import net.minecraft.block.BlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.world.IBlockReader

open class BlockSimpleShaped(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimple(builder){
	private val shape = aabb.asVoxelShape
	override fun getShape(state: BlockState, source: IBlockReader, pos: BlockPos, context: ISelectionContext) = shape
}
