package chylex.hee.game.block.util
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks

object FutureBlocks{
	private fun <T : Comparable<T>> Block.withProperty(property: IProperty<T>, value: T): IBlockState{
		return this.defaultState.withProperty(property, value)
	}
}
