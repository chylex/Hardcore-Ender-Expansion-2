package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FlowerPotBlock

open class BlockFlowerPotCustom(builder: BlockBuilder, flower: Block) : FlowerPotBlock(supply(Blocks.FLOWER_POT as FlowerPotBlock), supply(flower), builder.p), IHeeBlock {
	override val renderLayer
		get() = CUTOUT
	
	init {
		@Suppress("LeakingThis")
		(Blocks.FLOWER_POT as FlowerPotBlock).addPlant(flower.registryName!!, supply(this))
	}
}
