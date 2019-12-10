package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.getBlock
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class BlockVoidPortalCrafted(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb){
	override fun getBlockHardness(state: IBlockState, world: World, pos: BlockPos): Float{
		return if (Facing4.any { pos.offset(it).getBlock(world) === ModBlocks.VOID_PORTAL_INNER })
			blockHardness * 20F
		else
			blockHardness
	}
}
