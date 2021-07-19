package chylex.hee.datagen.client.util

import chylex.hee.datagen.safeUnit
import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.util.CAULDRON_LEVEL
import chylex.hee.system.path
import net.minecraft.block.Block
import net.minecraft.block.RotatedPillarBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.WallBlock
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ConfiguredModel
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile

fun BlockStateProvider.simpleStateOnly(block: Block, model: Block = block) = safeUnit {
	this.simpleBlock(block, UncheckedModelFile(model.location))
}

fun BlockStateProvider.pillar(block: RotatedPillarBlock, model: Block = block) = safeUnit {
	this.axisBlock(block, model.location)
}

fun BlockStateProvider.stairs(stairsBlock: StairsBlock, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	val texture = fullBlock.location
	this.stairsBlock(stairsBlock, side ?: texture, texture, texture)
}

fun BlockStateProvider.slab(slabBlock: SlabBlock, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	val texture = fullBlock.location
	this.slabBlock(slabBlock, texture, side ?: texture, texture, texture)
}

fun BlockStateProvider.wall(block: WallBlock, fullBlock: Block) = safeUnit {
	val texture = fullBlock.location
	this.wallBlock(block, texture)
	models().simple(block.suffixed("_inventory"), Resource.Vanilla("block/wall_inventory"), "wall", texture)
}

fun BlockStateProvider.cauldron(block: Block, water: ResourceLocation) = safeUnit {
	val models = models()
	val level1 = models.getBuilder(block.path + "_level1").parent(models.getExistingFile(Resource.Vanilla("block/cauldron_level1"))).texture("water", water)
	val level2 = models.getBuilder(block.path + "_level2").parent(models.getExistingFile(Resource.Vanilla("block/cauldron_level2"))).texture("water", water)
	val level3 = models.getBuilder(block.path + "_level3").parent(models.getExistingFile(Resource.Vanilla("block/cauldron_level3"))).texture("water", water)
	
	getVariantBuilder(block)
		.partialState().with(CAULDRON_LEVEL, 0).addModels(ConfiguredModel(models.getExistingFile(Resource.Vanilla("block/cauldron"))))
		.partialState().with(CAULDRON_LEVEL, 1).addModels(ConfiguredModel(level1))
		.partialState().with(CAULDRON_LEVEL, 2).addModels(ConfiguredModel(level2))
		.partialState().with(CAULDRON_LEVEL, 3).addModels(ConfiguredModel(level3))
}

fun BlockStateProvider.log(block: RotatedPillarBlock) = safeUnit {
	this.logBlock(block)
}
