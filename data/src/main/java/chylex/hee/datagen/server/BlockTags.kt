package chylex.hee.datagen.server

import chylex.hee.datagen.server.util.add
import chylex.hee.game.block.BlockWhitebarkSapling
import chylex.hee.init.ModBlocks
import chylex.hee.system.forge.getRegistryEntries
import chylex.hee.system.migration.BlockFlowerPot
import chylex.hee.system.migration.BlockLeaves
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.BlockWall
import net.minecraft.block.Block
import net.minecraft.data.BlockTagsProvider
import net.minecraft.data.DataGenerator
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.Tags

class BlockTags(generator: DataGenerator) : BlockTagsProvider(generator) {
	private val blocks = getRegistryEntries<Block>(ModBlocks)
	
	override fun registerTags() {
		getBuilder(BlockTags.BAMBOO_PLANTABLE_ON).add(ModBlocks.HUMUS)
		getBuilder(BlockTags.FLOWER_POTS).add(blocks.filterIsInstance<BlockFlowerPot>())
		getBuilder(BlockTags.IMPERMEABLE).add(ModBlocks.INFUSED_GLASS)
		getBuilder(BlockTags.LEAVES).add(blocks.filterIsInstance<BlockLeaves>())
		getBuilder(BlockTags.LOGS).add(ModBlocks.WHITEBARK_LOG, ModBlocks.WHITEBARK)
		getBuilder(BlockTags.PORTALS).add(ModBlocks.END_PORTAL_INNER, ModBlocks.VOID_PORTAL_INNER)
		getBuilder(BlockTags.PLANKS).add(ModBlocks.WHITEBARK_PLANKS)
		getBuilder(BlockTags.SAPLINGS).add(blocks.filterIsInstance<BlockWhitebarkSapling>())
		getBuilder(BlockTags.SLABS).add(blocks.filterIsInstance<BlockSlab>())
		getBuilder(BlockTags.STAIRS).add(blocks.filterIsInstance<BlockStairs>())
		getBuilder(BlockTags.WALLS).add(blocks.filterIsInstance<BlockWall>())
		getBuilder(BlockTags.WOODEN_SLABS).add(ModBlocks.WHITEBARK_SLAB)
		getBuilder(BlockTags.WOODEN_STAIRS).add(ModBlocks.WHITEBARK_STAIRS)
		
		getBuilder(Tags.Blocks.CHESTS).add(ModBlocks.DARK_CHEST)
		getBuilder(Tags.Blocks.END_STONES).add(ModBlocks.END_STONE_INFESTED, ModBlocks.END_STONE_BURNED, ModBlocks.END_STONE_ENCHANTED)
		getBuilder(Tags.Blocks.GLASS).add(ModBlocks.INFUSED_GLASS)
		getBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_FALLING)
		getBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_SMOOTH, ModBlocks.OBSIDIAN_CHISELED, ModBlocks.OBSIDIAN_PILLAR)
		getBuilder(Tags.Blocks.OBSIDIAN).add(ModBlocks.OBSIDIAN_SMOOTH_LIT, ModBlocks.OBSIDIAN_CHISELED_LIT, ModBlocks.OBSIDIAN_PILLAR_LIT)
		getBuilder(Tags.Blocks.ORES).add(ModBlocks.END_POWDER_ORE, ModBlocks.ENDIUM_ORE, ModBlocks.STARDUST_ORE, ModBlocks.IGNEOUS_ROCK_ORE)
		getBuilder(Tags.Blocks.STORAGE_BLOCKS).add(ModBlocks.ENDIUM_BLOCK)
	}
}
