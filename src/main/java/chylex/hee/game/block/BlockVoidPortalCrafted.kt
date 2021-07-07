package chylex.hee.game.block

import chylex.hee.game.block.logic.IBlockDynamicHardness
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.getBlock
import chylex.hee.init.ModBlocks
import net.minecraft.block.BlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

open class BlockVoidPortalCrafted(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb), IBlockDynamicHardness {
	override fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float {
		return if (Facing4.any { pos.offset(it).getBlock(world) === ModBlocks.VOID_PORTAL_INNER })
			originalHardness * 20F
		else
			originalHardness
	}
	
	override fun getTranslationKey(): String {
		return ModBlocks.VOID_PORTAL_FRAME.translationKey
	}
}
