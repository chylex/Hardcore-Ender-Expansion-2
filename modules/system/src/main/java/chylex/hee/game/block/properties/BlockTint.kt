package chylex.hee.game.block.properties

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader

@Sided(Side.CLIENT, _interface = IBlockColor::class)
abstract class BlockTint : IBlockColor {
	protected companion object {
		const val NO_TINT = -1
	}
	
	@Sided(Side.CLIENT)
	open fun forItem(block: Block): IItemColor? {
		return IItemColor { _, tintIndex -> this@BlockTint.tint(block.defaultState, null, null, tintIndex) }
	}
	
	@Sided(Side.CLIENT)
	final override fun getColor(state: BlockState, world: IBlockDisplayReader?, pos: BlockPos?, tintIndex: Int): Int {
		return tint(state, world, pos, tintIndex)
	}
	
	@Sided(Side.CLIENT)
	abstract fun tint(state: BlockState, world: IBlockDisplayReader?, pos: BlockPos?, tintIndex: Int): Int
}
