package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.IBlockStateModel
import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FlowerPotBlock
import net.minecraft.tags.BlockTags

open class BlockFlowerPotCustom protected constructor(
	builder: BlockBuilder,
	flower: Block,
	registerPlant: Boolean,
) : FlowerPotBlock(
	supply(Blocks.FLOWER_POT as FlowerPotBlock), /* prevents adding to flower->pot map */
	supply(flower),
	builder.p
), IHeeBlock {
	constructor(builder: BlockBuilder, flower: Block) : this(builder, flower, registerPlant = true)
	
	override val localization: LocalizationStrategy
		get() = (flower as? IHeeBlock)?.localization ?: super.localization
	
	override val model: IBlockStateModel
		get() = BlockStateModels.PottedPlant(flower)
	
	final override val renderLayer
		get() = CUTOUT
	
	override val drop: BlockDrop
		get() = BlockDrop.FlowerPot
	
	final override val tags
		get() = listOf(BlockTags.FLOWER_POTS)
	
	init {
		if (registerPlant) {
			@Suppress("LeakingThis")
			(Blocks.FLOWER_POT as FlowerPotBlock).addPlant(flower.registryName!!, supply(this))
		}
	}
}
