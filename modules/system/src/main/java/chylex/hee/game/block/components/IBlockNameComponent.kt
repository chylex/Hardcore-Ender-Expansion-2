package chylex.hee.game.block.components

import net.minecraft.block.Block

interface IBlockNameComponent {
	val translationKey: String
	
	companion object {
		fun of(translationKey: String) = object : IBlockNameComponent {
			override val translationKey
				get() = translationKey
		}
		
		fun of(block: Block) = object : IBlockNameComponent {
			override val translationKey
				get() = block.translationKey
		}
	}
}
