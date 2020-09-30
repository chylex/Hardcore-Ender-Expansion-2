package chylex.hee.datagen.client.util
import chylex.hee.datagen.Callback
import chylex.hee.datagen.isVanilla
import chylex.hee.datagen.path
import chylex.hee.datagen.r
import chylex.hee.datagen.resource
import chylex.hee.datagen.safe
import chylex.hee.datagen.safeUnit
import chylex.hee.datagen.then
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.named
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.BlockWall
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockModelProvider

fun Block.suffixed(suffix: String): Block{
	return Block(Block.Properties.from(Blocks.AIR)) named this.path + suffix
}

fun BlockModelProvider.parent(path: String, parent: ResourceLocation) = safe {
	this.getBuilder(path).parent(getExistingFile(parent))
}

fun BlockModelProvider.parent(block: Block, parent: ResourceLocation) = safe {
	this.getBuilder(block.path).parent(getExistingFile(parent))
}

fun BlockModelProvider.simple(block: Block, parent: ResourceLocation, textureName: String, textureLocation: ResourceLocation = block.r): BlockModelBuilder?{
	return this.parent(block, parent).then { texture(textureName, textureLocation) }
}

fun BlockModelProvider.cube(block: Block, texture: ResourceLocation = block.r): BlockModelBuilder?{
	return this.simple(block, Resource.Vanilla("block/cube_all"), "all", texture)
}

fun BlockModelProvider.cross(block: Block, texture: ResourceLocation = block.r): BlockModelBuilder?{
	return this.simple(block, Resource.Vanilla("block/cross"), "cross", texture)
}

fun BlockModelProvider.cubeColumn(block: Block, side: ResourceLocation = block.r, end: ResourceLocation = block.r("_top")) = safe {
	return this.cubeColumn(block.path, side, end)
}

fun BlockModelProvider.cubeBottomTop(block: Block, side: ResourceLocation = block.r("_side"), bottom: ResourceLocation = block.r("_bottom"), top: ResourceLocation = block.r("_top")) = safe {
	return this.cubeBottomTop(block.path, side, bottom, top)
}

fun BlockModelProvider.leaves(block: Block): BlockModelBuilder?{
	return this.simple(block, Resource.Vanilla("block/leaves"), "all")
}

fun BlockModelProvider.particle(block: Block, particle: ResourceLocation) = safe {
	this.getBuilder(block.path).texture("particle", particle)
}

fun BlockModelProvider.multi(block: Block, parent: ResourceLocation, suffixes: Array<String>, callback: BlockModelBuilder.(Callback<Block>) -> Unit){
	for(suffix in suffixes){
		val path = block.path + suffix
		
		this.safeUnit {
			this.getBuilder(path).parent(getExistingFile(parent)).callback(Callback(block, suffix, path))
		}
	}
}

fun BlockModelProvider.multi(block: Block, parent: ResourceLocation, suffixes: IntRange, callback: BlockModelBuilder.(Callback<Block>) -> Unit){
	multi(block, parent, Array(1 + suffixes.last - suffixes.first){ "_${suffixes.first + it}" }, callback)
}

fun BlockModelProvider.stairs(stairBlock: BlockStairs, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	resource("block/" + fullBlock.path, fullBlock.isVanilla).let {
		stairs(stairBlock.path, side ?: it, it, it)
		stairsInner(stairBlock.path + "_inner", side ?: it, it, it)
		stairsOuter(stairBlock.path + "_outer", side ?: it, it, it)
	}
}

fun BlockModelProvider.slab(slabBlock: BlockSlab, fullBlock: Block, side: ResourceLocation? = null) = safeUnit {
	resource("block/" + fullBlock.path, fullBlock.isVanilla).let {
		slab(slabBlock.path, side ?: it, it, it)
		slabTop(slabBlock.path + "_top", side ?: it, it, it)
	}
}

fun BlockModelProvider.wall(block: BlockWall, texture: ResourceLocation) = safeUnit {
	wallPost(block.path + "_post", texture)
	wallSide(block.path + "_side", texture)
}

fun BlockModelProvider.cauldron(block: Block, water: ResourceLocation) = safeUnit {
	getBuilder(block.path + "_level1").parent(getExistingFile(Resource.Vanilla("block/cauldron_level1"))).texture("water", water)
	getBuilder(block.path + "_level2").parent(getExistingFile(Resource.Vanilla("block/cauldron_level2"))).texture("water", water)
	getBuilder(block.path + "_level3").parent(getExistingFile(Resource.Vanilla("block/cauldron_level3"))).texture("water", water)
}

fun BlockModelProvider.flowerPot(potBlock: Block, plantBlock: Block) = safe {
	this.getBuilder(potBlock.path)
		.parent(getExistingFile(Resource.Vanilla("block/flower_pot_cross")))
		.texture("plant", plantBlock.r)
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
