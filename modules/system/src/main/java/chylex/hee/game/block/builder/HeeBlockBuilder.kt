package chylex.hee.game.block.builder

import chylex.hee.game.block.HeeBlock2
import chylex.hee.game.block.HeeBlockWithComponents
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.interfaces.IBlockWithInterfaces
import net.minecraft.block.AbstractBlock.Properties
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateContainer.Builder

open class HeeBlockBuilder : AbstractHeeBlockBuilder<HeeBlock2>() {
	override fun buildBlock(properties: Properties, components: HeeBlockComponents?): HeeBlock2 {
		if (components != null) {
			return object : HeeBlockWithComponents(properties, components), IHeeBlock by IHeeBlock.FromBuilder(this@HeeBlockBuilder), IBlockWithInterfaces by interfaces.delegate {
				override fun fillStateContainer(builder: Builder<Block, BlockState>) {
					components.states.fillContainer(builder)
				}
			}
		}
		
		return object : HeeBlock2(properties), IHeeBlock by IHeeBlock.FromBuilder(this), IBlockWithInterfaces by interfaces.delegate {}
	}
}

inline fun HeeBlockBuilder(setup: HeeBlockBuilder.() -> Unit) = HeeBlockBuilder().apply(setup)
