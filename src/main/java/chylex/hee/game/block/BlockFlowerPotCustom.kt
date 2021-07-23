package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FlowerPotBlock

open class BlockFlowerPotCustom(builder: BlockBuilder, flower: Block) : FlowerPotBlock(supply(Blocks.FLOWER_POT as FlowerPotBlock), supply(flower), builder.p), IHeeBlock {
	final override val model
		get() = BlockStateModels.PottedPlant(flower)
	
	final override val renderLayer
		get() = CUTOUT
	
	final override val drop
		get() = BlockDrop.FlowerPot
	
	init {
		@Suppress("LeakingThis")
		(Blocks.FLOWER_POT as FlowerPotBlock).addPlant(flower.registryName!!, supply(this))
	}
}
