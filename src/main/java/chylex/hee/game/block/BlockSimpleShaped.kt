package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

open class BlockSimpleShaped(builder: BlockBuilder, private val aabb: AxisAlignedBB) : BlockSimple(builder){
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = aabb
	
	override fun isFullCube(state: IBlockState) = false
	override fun isOpaqueCube(state: IBlockState) = false
}
