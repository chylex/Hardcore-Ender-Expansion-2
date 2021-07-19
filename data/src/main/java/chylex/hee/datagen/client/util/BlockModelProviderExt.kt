package chylex.hee.datagen.client.util

import chylex.hee.datagen.safe
import chylex.hee.datagen.then
import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.system.named
import chylex.hee.system.path
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockModelProvider

fun Block.suffixed(suffix: String): Block {
	return Block(AbstractBlock.Properties.from(Blocks.AIR)) named this.path + suffix
}

fun BlockModelProvider.parent(path: String, parent: ResourceLocation) = safe {
	this.getBuilder(path).parent(getExistingFile(parent))
}

fun BlockModelProvider.parent(block: Block, parent: ResourceLocation) = safe {
	this.getBuilder(block.path).parent(getExistingFile(parent))
}

fun BlockModelProvider.simple(block: Block, parent: ResourceLocation, textureName: String, textureLocation: ResourceLocation = block.location): BlockModelBuilder? {
	return this.parent(block, parent).then { texture(textureName, textureLocation) }
}

fun BlockModelProvider.cubeColumn(block: Block, side: ResourceLocation = block.location, end: ResourceLocation = block.location("_top")) = safe {
	return this.cubeColumn(block.path, side, end)
}

fun BlockModelProvider.cubeBottomTop(block: Block, side: ResourceLocation, bottom: ResourceLocation, top: ResourceLocation) = safe {
	return this.cubeBottomTop(block.path, side, bottom, top)
}

fun BlockModelProvider.particle(block: Block, particle: ResourceLocation) = safe {
	this.getBuilder(block.path).texture("particle", particle)
}

fun BlockModelProvider.portalFrame(block: Block, side: ResourceLocation, top: ResourceLocation) = safe {
	this.getBuilder(block.path)
		.parent(getExistingFile(Resource.Custom("block/portal_frame")))
		.texture("particle", side)
		.texture("side", side)
		.texture("top", top)
		.texture("bottom", Resource.Vanilla("block/end_stone"))
}

fun BlockModelProvider.table(block: BlockAbstractTable) = safe {
	this.getBuilder(block.path)
		.parent(getExistingFile(Resource.Custom("block/table_tier_" + block.tier)))
		.texture("overlay_top", Resource.Custom("block/" + block.path.substringBeforeLast("_tier") + "_top"))
		.texture("overlay_side", Resource.Custom("block/" + block.path.substringBeforeLast("_tier") + "_side"))
}
