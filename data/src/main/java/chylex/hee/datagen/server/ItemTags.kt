package chylex.hee.datagen.server

import chylex.hee.init.ModItems
import net.minecraft.data.BlockTagsProvider
import net.minecraft.data.DataGenerator
import net.minecraft.data.ItemTagsProvider
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.ExistingFileHelper

class ItemTags(dataGenerator: DataGenerator, blockTagProvider: BlockTagsProvider, modId: String, existingFileHelper: ExistingFileHelper?) : ItemTagsProvider(dataGenerator, blockTagProvider, modId, existingFileHelper) {
	override fun registerTags() {
		copy(BlockTags.LEAVES, ItemTags.LEAVES)
		copy(BlockTags.LOGS, ItemTags.LOGS)
		copy(BlockTags.PLANKS, ItemTags.PLANKS)
		copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS)
		copy(BlockTags.SLABS, ItemTags.SLABS)
		copy(BlockTags.STAIRS, ItemTags.STAIRS)
		copy(BlockTags.WALLS, ItemTags.WALLS)
		copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS)
		copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS)
		
		copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS)
		copy(Tags.Blocks.END_STONES, Tags.Items.END_STONES)
		copy(Tags.Blocks.GLASS, Tags.Items.GLASS)
		copy(Tags.Blocks.OBSIDIAN, Tags.Items.OBSIDIAN)
		copy(Tags.Blocks.ORES, Tags.Items.ORES)
		copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS)
		
		getOrCreateBuilder(Tags.Items.DUSTS).add(ModItems.ANCIENT_DUST, ModItems.END_POWDER, ModItems.STARDUST)
		getOrCreateBuilder(Tags.Items.ENDER_PEARLS).add(ModItems.INFUSED_ENDER_PEARL)
		getOrCreateBuilder(Tags.Items.HEADS).add(ModItems.ENDERMAN_HEAD)
		getOrCreateBuilder(Tags.Items.INGOTS).add(ModItems.ENDIUM_INGOT, ModItems.INFERNIUM_INGOT, ModItems.DIRTY_INFERNIUM_INGOT)
		getOrCreateBuilder(Tags.Items.NUGGETS).add(ModItems.ENDIUM_NUGGET)
		getOrCreateBuilder(Tags.Items.RODS).add(ModItems.OBSIDIAN_ROD)
	}
}
