package chylex.hee.game.block
import chylex.hee.HEE
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockScaffolding(builder: BlockSimple.Builder) : BlockSimple(builder){
	override fun isBlockNormalCube(state: IBlockState): Boolean = false
	override fun isNormalCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	
	override fun canCollideCheck(state: IBlockState, hitIfLiquid: Boolean): Boolean{
		val player = HEE.proxy.getClientSidePlayer()
		return player == null || player.capabilities.isFlying
	}
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB?{
		val player = HEE.proxy.getClientSidePlayer()
		
		return if (player == null || !player.capabilities.isFlying)
			FULL_BLOCK_AABB
		else
			NULL_AABB
	}
	
	override fun canPlaceTorchOnTop(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = true
	override fun causesSuffocation(state: IBlockState): Boolean = false
	override fun getRenderLayer(): BlockRenderLayer = CUTOUT
}
