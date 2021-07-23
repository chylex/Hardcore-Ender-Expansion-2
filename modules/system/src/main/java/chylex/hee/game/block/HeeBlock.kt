package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import net.minecraft.block.Block
import net.minecraft.loot.LootTables

open class HeeBlock(builder: BlockBuilder) : Block(builder.p), IHeeBlock {
	override val drop
		get() = if (lootTable == LootTables.EMPTY) BlockDrop.Nothing else BlockDrop.Self
}
