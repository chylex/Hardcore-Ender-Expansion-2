package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.cubeBottomTop
import chylex.hee.datagen.client.util.cubeColumn
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.particle
import chylex.hee.datagen.client.util.portalFrame
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.client.util.suffixed
import chylex.hee.datagen.client.util.table
import chylex.hee.datagen.then
import chylex.hee.game.Resource.location
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockModel.CubeBottomTop
import chylex.hee.game.block.properties.BlockModel.CubeColumn
import chylex.hee.game.block.properties.BlockModel.Fluid
import chylex.hee.game.block.properties.BlockModel.FromParent
import chylex.hee.game.block.properties.BlockModel.Manual
import chylex.hee.game.block.properties.BlockModel.Multi
import chylex.hee.game.block.properties.BlockModel.NoAmbientOcclusion
import chylex.hee.game.block.properties.BlockModel.Parent
import chylex.hee.game.block.properties.BlockModel.ParticleOnly
import chylex.hee.game.block.properties.BlockModel.PortalFrame
import chylex.hee.game.block.properties.BlockModel.SimpleBlockModel
import chylex.hee.game.block.properties.BlockModel.Suffixed
import chylex.hee.game.block.properties.BlockModel.Table
import chylex.hee.game.block.properties.BlockModel.WithTextures
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class BlockModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : BlockModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		for (block in ModBlocks.ALL) {
			(block as? IHeeBlock)?.let { registerModel(block, it.model.generate(block).blockModel) { builder -> builder } }
		}
	}
	
	private fun registerModel(block: Block, model: BlockModel, callback: (BlockModelBuilder) -> BlockModelBuilder) {
		when (model) {
			is SimpleBlockModel -> simple(block, model.parent, model.textureName, model.textureLocation ?: block.location)?.then(callback)
			is CubeBottomTop    -> cubeBottomTop(block, model.side ?: block.location("_side"), model.bottom ?: block.location("_bottom"), model.top ?: block.location("_top"))?.then(callback)
			is PortalFrame      -> portalFrame(block, model.frameBlock.location("_side"), model.frameBlock.location("_top_" + model.topSuffix))?.then(callback)
			is ParticleOnly     -> particle(block, model.particle)?.then(callback)
			is Parent           -> parent(model.name, model.parent)?.then(callback)
			is FromParent       -> parent(block, model.parent)?.then(callback)
			is Suffixed         -> registerModel(block.suffixed(model.suffix), model.wrapped, callback)
			
			is WithTextures -> registerModel(block, model.baseModel) {
				model.textures.entries.fold(callback(it)) { builder, (name, location) -> builder.texture(name, location) }
			}
			
			is NoAmbientOcclusion -> registerModel(block, model.baseModel) {
				callback(it).ao(false)
			}
			
			is Multi -> {
				for (innerModel in model.models) {
					registerModel(block, innerModel, callback)
				}
			}
			
			CubeColumn -> cubeColumn(block)?.then(callback)
			Table      -> table(block as BlockAbstractTable)?.then(callback)
			Fluid      -> particle(block, block.location("_still"))?.then(callback)
			Manual     -> return
		}
	}
}
