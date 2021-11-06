package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.state.properties.BlockStateProperties

interface IFlammableBlockComponent {
	val flammability: Int
	val fireSpread: Int
	
	fun takeIfFlammable(state: BlockState): IFlammableBlockComponent? {
		return this.takeUnless { state.hasProperty(BlockStateProperties.WATERLOGGED) && state[BlockStateProperties.WATERLOGGED] }
	}
	
	companion object {
		fun of(flammability: Int, fireSpread: Int) = object : IFlammableBlockComponent {
			override val flammability = flammability
			override val fireSpread = fireSpread
		}
	}
}
