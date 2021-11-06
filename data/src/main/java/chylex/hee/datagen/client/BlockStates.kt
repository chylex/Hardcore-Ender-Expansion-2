package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.cauldron
import chylex.hee.datagen.client.util.log
import chylex.hee.datagen.client.util.pillar
import chylex.hee.datagen.client.util.simpleStateOnly
import chylex.hee.datagen.client.util.slab
import chylex.hee.datagen.client.util.stairs
import chylex.hee.datagen.client.util.wall
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.BlockStatePreset.Cauldron
import chylex.hee.game.block.properties.BlockStatePreset.Log
import chylex.hee.game.block.properties.BlockStatePreset.None
import chylex.hee.game.block.properties.BlockStatePreset.Pillar
import chylex.hee.game.block.properties.BlockStatePreset.PillarFrom
import chylex.hee.game.block.properties.BlockStatePreset.Simple
import chylex.hee.game.block.properties.BlockStatePreset.SimpleFrom
import chylex.hee.game.block.properties.BlockStatePreset.Slab
import chylex.hee.game.block.properties.BlockStatePreset.Stairs
import chylex.hee.game.block.properties.BlockStatePreset.Wall
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block
import net.minecraft.block.RotatedPillarBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.WallBlock
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.common.data.ExistingFileHelper

class BlockStates(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : BlockStateProvider(generator, modid, existingFileHelper) {
	override fun registerStatesAndModels() {
		for (block in ModBlocks.ALL) {
			(block as? IHeeBlock)?.let { registerState(block, it.model.generate(block).blockState) }
		}
	}
	
	private fun registerState(block: Block, model: BlockStatePreset) {
		when (model) {
			None          -> return
			Simple        -> simpleStateOnly(block)
			is SimpleFrom -> simpleStateOnly(block, model.modelBlock)
			Pillar        -> pillar(block as RotatedPillarBlock)
			is PillarFrom -> pillar(block as RotatedPillarBlock, model.modelBlock)
			is Stairs     -> stairs(block as StairsBlock, model.fullBlock, model.side)
			is Slab       -> slab(block as SlabBlock, model.fullBlock, model.side)
			is Wall       -> wall(block as WallBlock, model.fullBlock)
			is Cauldron   -> cauldron(block, model.fluidTexture)
			Log           -> log(block as RotatedPillarBlock)
		}
	}
}
