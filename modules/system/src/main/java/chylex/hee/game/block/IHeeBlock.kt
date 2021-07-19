package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockTint

interface IHeeBlock {
	val tint: BlockTint?
		get() = null
}
