package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.cube
import chylex.hee.datagen.client.util.log
import chylex.hee.datagen.client.util.pillar
import chylex.hee.datagen.client.util.simpleStateAndItem
import chylex.hee.datagen.client.util.simpleStateOnly
import chylex.hee.datagen.client.util.slab
import chylex.hee.datagen.client.util.stairs
import chylex.hee.datagen.r
import chylex.hee.init.ModBlocks
import net.minecraft.block.Blocks
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ExistingFileHelper

class BlockStates(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : BlockStateProvider(generator, modid, existingFileHelper) {
	override fun registerStatesAndModels() {
		
		// Blocks: Building (Uncategorized)
		
		cube(ModBlocks.ETHEREAL_LANTERN)
		cube(ModBlocks.VANTABLOCK)
		cube(ModBlocks.ENDIUM_BLOCK)
		
		// Blocks: Building (Gloomrock)
		
		cube(ModBlocks.GLOOMROCK)
		cube(ModBlocks.GLOOMROCK_BRICKS)
		stairs(ModBlocks.GLOOMROCK_BRICK_STAIRS, ModBlocks.GLOOMROCK_BRICKS)
		slab(ModBlocks.GLOOMROCK_BRICK_SLAB, ModBlocks.GLOOMROCK_BRICKS)
		cube(ModBlocks.GLOOMROCK_SMOOTH)
		stairs(ModBlocks.GLOOMROCK_SMOOTH_STAIRS, ModBlocks.GLOOMROCK_SMOOTH, side = ModBlocks.GLOOMROCK_SMOOTH_SLAB.r("_side"))
		slab(ModBlocks.GLOOMROCK_SMOOTH_SLAB, ModBlocks.GLOOMROCK_SMOOTH, side = ModBlocks.GLOOMROCK_SMOOTH_SLAB.r("_side"))
		cube(ModBlocks.GLOOMROCK_SMOOTH_RED)
		cube(ModBlocks.GLOOMROCK_SMOOTH_ORANGE)
		cube(ModBlocks.GLOOMROCK_SMOOTH_YELLOW)
		cube(ModBlocks.GLOOMROCK_SMOOTH_GREEN)
		cube(ModBlocks.GLOOMROCK_SMOOTH_CYAN)
		cube(ModBlocks.GLOOMROCK_SMOOTH_BLUE)
		cube(ModBlocks.GLOOMROCK_SMOOTH_PURPLE)
		cube(ModBlocks.GLOOMROCK_SMOOTH_MAGENTA)
		cube(ModBlocks.GLOOMROCK_SMOOTH_WHITE)
		
		// Blocks: Building (Dusty Stone)
		
		cube(ModBlocks.DUSTY_STONE)
		cube(ModBlocks.DUSTY_STONE_CRACKED)
		cube(ModBlocks.DUSTY_STONE_DAMAGED)
		cube(ModBlocks.DUSTY_STONE_BRICKS)
		cube(ModBlocks.DUSTY_STONE_CRACKED_BRICKS)
		cube(ModBlocks.DUSTY_STONE_DECORATION)
		stairs(ModBlocks.DUSTY_STONE_BRICK_STAIRS, ModBlocks.DUSTY_STONE_BRICKS)
		slab(ModBlocks.DUSTY_STONE_BRICK_SLAB, ModBlocks.DUSTY_STONE_BRICKS)
		
		// Blocks: Building (Obsidian)
		
		stairs(ModBlocks.OBSIDIAN_STAIRS, Blocks.OBSIDIAN)
		simpleStateAndItem(ModBlocks.OBSIDIAN_FALLING)
		cube(ModBlocks.OBSIDIAN_SMOOTH)
		cube(ModBlocks.OBSIDIAN_CHISELED)
		pillar(ModBlocks.OBSIDIAN_PILLAR)
		cube(ModBlocks.OBSIDIAN_SMOOTH_LIT, ModBlocks.OBSIDIAN_SMOOTH)
		cube(ModBlocks.OBSIDIAN_CHISELED_LIT, ModBlocks.OBSIDIAN_CHISELED)
		pillar(ModBlocks.OBSIDIAN_PILLAR_LIT, ModBlocks.OBSIDIAN_PILLAR)
		cube(ModBlocks.OBSIDIAN_TOWER_TOP, ModBlocks.OBSIDIAN_CHISELED)
		
		// Blocks: Building (End Stone)
		
		simpleStateAndItem(ModBlocks.END_STONE_INFESTED)
		simpleStateAndItem(ModBlocks.END_STONE_BURNED)
		simpleStateAndItem(ModBlocks.END_STONE_ENCHANTED)
		
		// Blocks: Building (Dark Loam)
		
		cube(ModBlocks.DARK_LOAM)
		slab(ModBlocks.DARK_LOAM_SLAB, ModBlocks.DARK_LOAM)
		
		// Blocks: Building (Wood)
		
		log(ModBlocks.WHITEBARK_LOG)
		simpleStateAndItem(ModBlocks.WHITEBARK)
		cube(ModBlocks.WHITEBARK_PLANKS)
		stairs(ModBlocks.WHITEBARK_STAIRS, ModBlocks.WHITEBARK_PLANKS)
		slab(ModBlocks.WHITEBARK_SLAB, ModBlocks.WHITEBARK_PLANKS)
		
		// Blocks: Building (Miner's Burial)
		
		cube(ModBlocks.MINERS_BURIAL_BLOCK_PLAIN)
		cube(ModBlocks.MINERS_BURIAL_BLOCK_CHISELED)
		pillar(ModBlocks.MINERS_BURIAL_BLOCK_PILLAR)
		cube(ModBlocks.MINERS_BURIAL_BLOCK_JAIL)
		simpleStateAndItem(ModBlocks.MINERS_BURIAL_ALTAR)
		
		// Blocks: Fluids
		
		simpleStateOnly(ModBlocks.ENDER_GOO)
		simpleStateOnly(ModBlocks.PURIFIED_ENDER_GOO)
		
		// Blocks: Interactive (Storage)
		
		simpleStateOnly(ModBlocks.JAR_O_DUST)
		simpleStateOnly(ModBlocks.DARK_CHEST)
		simpleStateOnly(ModBlocks.LOOT_CHEST)
		
		// Blocks: Interactive (Puzzle)
		
		cube(ModBlocks.PUZZLE_WALL)
		
		// Blocks: Interactive (Gates)
		
		simpleStateAndItem(ModBlocks.EXPERIENCE_GATE_CONTROLLER)
		
		// Blocks: Interactive (Uncategorized)
		
		simpleStateAndItem(ModBlocks.INFUSED_TNT)
		simpleStateOnly(ModBlocks.IGNEOUS_PLATE)
		
		// Blocks: Ores
		
		cube(ModBlocks.END_POWDER_ORE)
		cube(ModBlocks.ENDIUM_ORE)
		cube(ModBlocks.IGNEOUS_ROCK_ORE)
		
		// Blocks: Decorative (Trees)
		
		simpleStateOnly(ModBlocks.WHITEBARK_SAPLING_AUTUMN_RED)
		simpleStateOnly(ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN)
		simpleStateOnly(ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE)
		simpleStateOnly(ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		simpleStateAndItem(ModBlocks.WHITEBARK_LEAVES_AUTUMN_RED)
		simpleStateAndItem(ModBlocks.WHITEBARK_LEAVES_AUTUMN_BROWN)
		simpleStateAndItem(ModBlocks.WHITEBARK_LEAVES_AUTUMN_ORANGE)
		simpleStateAndItem(ModBlocks.WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN)
		simpleStateOnly(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_RED)
		simpleStateOnly(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_BROWN)
		simpleStateOnly(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_ORANGE)
		simpleStateOnly(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		
		// Blocks: Decorative (Plants)
		
		simpleStateOnly(ModBlocks.DEATH_FLOWER_HEALED)
		simpleStateOnly(ModBlocks.DEATH_FLOWER_WITHERED)
		simpleStateOnly(ModBlocks.POTTED_DEATH_FLOWER_HEALED)
		simpleStateOnly(ModBlocks.POTTED_DEATH_FLOWER_WITHERED)
		
		// Blocks: Decorative (Uncategorized)
		
		simpleStateOnly(ModBlocks.ANCIENT_COBWEB)
		simpleStateOnly(ModBlocks.ENDERMAN_HEAD, Blocks.SOUL_SAND)
		simpleStateOnly(ModBlocks.ENDERMAN_WALL_HEAD, Blocks.SOUL_SAND)
		
		// Blocks: Spawners
		
		simpleStateOnly(ModBlocks.SPAWNER_OBSIDIAN_TOWERS, Blocks.SPAWNER)
		
		// Blocks: Portals
		
		simpleStateOnly(ModBlocks.END_PORTAL_INNER, Blocks.END_PORTAL)
		simpleStateAndItem(ModBlocks.END_PORTAL_FRAME)
		simpleStateAndItem(ModBlocks.END_PORTAL_ACCEPTOR)
		simpleStateOnly(ModBlocks.VOID_PORTAL_INNER, Blocks.END_PORTAL)
		simpleStateAndItem(ModBlocks.VOID_PORTAL_FRAME)
		simpleStateAndItem(ModBlocks.VOID_PORTAL_STORAGE)
		simpleStateAndItem(ModBlocks.VOID_PORTAL_FRAME_CRAFTED)
		simpleStateAndItem(ModBlocks.VOID_PORTAL_STORAGE_CRAFTED)
		
		// Blocks: Energy
		
		simpleStateAndItem(ModBlocks.CORRUPTED_ENERGY)
		
		// Blocks: Tables
		
		simpleStateAndItem(ModBlocks.TABLE_BASE_TIER_1)
		simpleStateAndItem(ModBlocks.TABLE_BASE_TIER_2)
		simpleStateAndItem(ModBlocks.TABLE_BASE_TIER_3)
		simpleStateAndItem(ModBlocks.ACCUMULATION_TABLE_TIER_1)
		simpleStateAndItem(ModBlocks.ACCUMULATION_TABLE_TIER_2)
		simpleStateAndItem(ModBlocks.ACCUMULATION_TABLE_TIER_3)
		simpleStateAndItem(ModBlocks.EXPERIENCE_TABLE_TIER_1)
		simpleStateAndItem(ModBlocks.EXPERIENCE_TABLE_TIER_2)
		simpleStateAndItem(ModBlocks.EXPERIENCE_TABLE_TIER_3)
		simpleStateAndItem(ModBlocks.INFUSION_TABLE_TIER_1)
		simpleStateAndItem(ModBlocks.INFUSION_TABLE_TIER_2)
		simpleStateAndItem(ModBlocks.INFUSION_TABLE_TIER_3)
		
		// Blocks: Utilities
		
		simpleStateAndItem(ModBlocks.SCAFFOLDING)
	}
}
