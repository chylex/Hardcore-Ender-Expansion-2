package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Facing4
import net.minecraft.block.BlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

open class BlockVoidPortalCrafted(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb) {
	override fun getBlockHardness(state: BlockState, world: IBlockReader, pos: BlockPos): Float {
		return if (Facing4.any { pos.offset(it).getBlock(world) === ModBlocks.VOID_PORTAL_INNER })
			blockHardness * 20F
		else
			blockHardness
	}
	
	override fun getTranslationKey(): String {
		return ModBlocks.VOID_PORTAL_FRAME.translationKey
	}
}
