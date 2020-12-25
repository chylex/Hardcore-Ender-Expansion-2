package chylex.hee.game.mechanics.scorching

import net.minecraft.block.BlockState
import net.minecraft.item.IItemTier

interface IScorchingItem {
	val material: IItemTier
	fun canMine(state: BlockState): Boolean
}
