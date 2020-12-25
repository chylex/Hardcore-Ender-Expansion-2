package chylex.hee.datagen.client.util

import chylex.hee.datagen.r
import chylex.hee.datagen.safeUnit
import chylex.hee.system.migration.BlockLog
import chylex.hee.system.migration.BlockRotatedPillar
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.BlockStairs
import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile

private fun BlockStateProvider.simpleBlockItem(block: Block) {
	this.simpleBlockItem(block, UncheckedModelFile(block.r))
}

fun BlockStateProvider.simpleStateOnly(block: Block, model: Block = block) = safeUnit {
	this.simpleBlock(block, UncheckedModelFile(model.r))
}

fun BlockStateProvider.simpleStateAndItem(block: Block) = safeUnit {
	this.simpleBlock(block, UncheckedModelFile(block.r))
	this.simpleBlockItem(block)
}

fun BlockStateProvider.cube(block: Block) = safeUnit {
	this.simpleBlock(block)
	this.simpleBlockItem(block)
}

fun BlockStateProvider.cube(block: Block, model: Block) = safeUnit {
	val modelFile = UncheckedModelFile(model.r)
	this.simpleBlock(block, modelFile)
	this.simpleBlockItem(block, modelFile)
}

fun BlockStateProvider.stairs(stairBlock: BlockStairs, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	val texture = fullBlock.r
	this.stairsBlock(stairBlock, side ?: texture, texture, texture)
	this.simpleBlockItem(stairBlock)
}

fun BlockStateProvider.slab(slabBlock: BlockSlab, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	val texture = fullBlock.r
	this.slabBlock(slabBlock, texture, side ?: texture, texture, texture)
	this.simpleBlockItem(slabBlock)
}

fun BlockStateProvider.log(block: BlockLog) = safeUnit {
	this.logBlock(block)
	this.simpleBlockItem(block)
}

fun BlockStateProvider.pillar(block: BlockRotatedPillar) = safeUnit {
	this.axisBlock(block)
	this.simpleBlockItem(block)
}

fun BlockStateProvider.pillar(block: BlockRotatedPillar, model: Block) = safeUnit {
	val modelFile = UncheckedModelFile(model.r)
	this.axisBlock(block, modelFile)
	this.simpleBlockItem(block, modelFile)
}
