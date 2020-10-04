package chylex.hee.datagen.server
import chylex.hee.init.ModItems
import net.minecraft.data.DataGenerator
import net.minecraft.data.ItemTagsProvider
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.Tags

class ItemTags(generator: DataGenerator) : ItemTagsProvider(generator){
	override fun registerTags(){
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
		
		getBuilder(Tags.Items.DUSTS).add(ModItems.ANCIENT_DUST, ModItems.END_POWDER, ModItems.STARDUST)
		getBuilder(Tags.Items.ENDER_PEARLS).add(ModItems.INFUSED_ENDER_PEARL)
		getBuilder(Tags.Items.HEADS).add(ModItems.ENDERMAN_HEAD)
		getBuilder(Tags.Items.INGOTS).add(ModItems.ENDIUM_INGOT, ModItems.INFERNIUM_INGOT, ModItems.DIRTY_INFERNIUM_INGOT)
		getBuilder(Tags.Items.NUGGETS).add(ModItems.ENDIUM_NUGGET)
		getBuilder(Tags.Items.RODS).add(ModItems.OBSIDIAN_ROD)
	}
}
