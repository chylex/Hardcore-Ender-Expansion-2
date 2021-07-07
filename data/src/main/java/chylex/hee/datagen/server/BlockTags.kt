package chylex.hee.datagen.server

import chylex.hee.datagen.server.util.add
import chylex.hee.game.block.BlockWhitebarkSapling
import chylex.hee.init.ModBlocks
import chylex.hee.system.getRegistryEntries
import net.minecraft.block.Block
import net.minecraft.block.FlowerPotBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.WallBlock
import net.minecraft.data.BlockTagsProvider
import net.minecraft.data.DataGenerator
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.ExistingFileHelper

class BlockTags(generator: DataGenerator, modId: String, existingFileHelper: ExistingFileHelper?) : BlockTagsProvider(generator, modId, existingFileHelper) {
	private val blocks = getRegistryEntries<Block>(ModBlocks)
	
	override fun registerTags() {
		getOrCreateBuilder(BlockTags.BAMBOO_PLANTABLE_ON).add(ModBlocks.HUMUS)
		getOrCreateBuilder(BlockTags.FLOWER_POTS).add(blocks.filterIsInstance<FlowerPotBlock>())
		getOrCreateBuilder(BlockTags.IMPERMEABLE).add(ModBlocks.INFUSED_GLASS)
		getOrCreateBuilder(BlockTags.LEAVES).add(blocks.filterIsInstance<LeavesBlock>())
		getOrCreateBuilder(BlockTags.LOGS).add(ModBlocks.WHITEBARK_LOG, ModBlocks.WHITEBARK)
		getOrCreateBuilder(BlockTags.PORTALS).add(ModBlocks.END_PORTAL_INNER, ModBlocks.VOID_PORTAL_INNER)
		getOrCreateBuilder(BlockTags.PLANKS).add(ModBlocks.WHITEBARK_PLANKS)
		getOrCreateBuilder(BlockTags.SAPLINGS).add(blocks.filterIsInstance<BlockWhitebarkSapling>())
		getOrCreateBuilder(BlockTags.SLABS).add(blocks.filterIsInstance<SlabBlock>())
		getOrCreateBuilder(BlockTags.STAIRS).add(blocks.filterIsInstance<StairsBlock>())
		getOrCreateBuilder(BlockTags.WALLS).add(blocks.filterIsInstance<WallBlock>())
		getOrCreateBuilder(BlockTags.WOODEN_SLABS).add(ModBlocks.WHITEBARK_SLAB)
		getOrCreateBuilder(BlockTags.WOODEN_STAIRS).add(ModBlocks.WHITEBARK_STAIRS)
		
		getOrCreateBuilder(Tags.Blocks.CHESTS).add(ModBlocks.DARK_CHEST)
		getOrCreateBuilder(Tags.Blocks.END_STONES).add(ModBlocks.END_STONE_INFESTED, ModBlocks.END_STONE_BURNED, ModBlocks.END_STONE_ENCHANTED)
		getOrCreateBuilder(Tags.Blocks.GLASS).add(ModBlocks.INFUSED_GLASS)
		getOrCreateBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_FALLING)
		getOrCreateBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_SMOOTH, ModBlocks.OBSIDIAN_CHISELED, ModBlocks.OBSIDIAN_PILLAR)
		getOrCreateBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_SMOOTH_LIT, ModBlocks.OBSIDIAN_CHISELED_LIT, ModBlocks.OBSIDIAN_PILLAR_LIT)
		getOrCreateBuilder(Tags.Blocks.ORES).add(ModBlocks.END_POWDER_ORE, ModBlocks.ENDIUM_ORE, ModBlocks.STARDUST_ORE, ModBlocks.IGNEOUS_ROCK_ORE)
		getOrCreateBuilder(Tags.Blocks.STORAGE_BLOCKS).add(ModBlocks.ENDIUM_BLOCK)
	}
}
