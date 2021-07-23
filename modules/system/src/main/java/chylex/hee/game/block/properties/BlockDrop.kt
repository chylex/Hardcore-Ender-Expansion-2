package chylex.hee.game.block.properties

import net.minecraft.util.IItemProvider

sealed class BlockDrop {
	object Nothing : BlockDrop()
	object Self : BlockDrop()
	object Manual : BlockDrop()
	object NamedTile : BlockDrop()
	object FlowerPot : BlockDrop()
	class OneOf(val item: IItemProvider) : BlockDrop()
}
