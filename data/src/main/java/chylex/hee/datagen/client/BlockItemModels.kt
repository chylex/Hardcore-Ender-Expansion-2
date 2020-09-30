package chylex.hee.datagen.client
import chylex.hee.datagen.client.util.block
import chylex.hee.datagen.client.util.multi
import chylex.hee.datagen.client.util.override
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.r
import chylex.hee.datagen.then
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Resource
import net.minecraft.block.Blocks
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.ExistingFileHelper
import net.minecraftforge.client.model.generators.ItemModelProvider

class BlockItemModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : ItemModelProvider(generator, modid, existingFileHelper){
	override fun registerModels(){
		
		// Blocks: Building (Uncategorized)
		
		block(ModBlocks.ETHEREAL_LANTERN)
		parent(ModBlocks.STONE_BRICK_WALL, Resource.Custom("block/stone_brick_wall_inventory"), checkExistence = false)
		parent(ModBlocks.INFUSED_GLASS, Resource.Custom("block/infused_glass_c0"))
		block(ModBlocks.VANTABLOCK)
		block(ModBlocks.ENDIUM_BLOCK)
		block(ModBlocks.ENDERSOL)
		block(ModBlocks.HUMUS)
		
		// Blocks: Building (Gloomrock)
		
		block(ModBlocks.GLOOMROCK)
		block(ModBlocks.GLOOMROCK_BRICKS)
		block(ModBlocks.GLOOMROCK_BRICK_STAIRS)
		block(ModBlocks.GLOOMROCK_BRICK_SLAB)
		block(ModBlocks.GLOOMROCK_SMOOTH)
		block(ModBlocks.GLOOMROCK_SMOOTH_STAIRS)
		block(ModBlocks.GLOOMROCK_SMOOTH_SLAB)
		block(ModBlocks.GLOOMROCK_SMOOTH_RED)
		block(ModBlocks.GLOOMROCK_SMOOTH_ORANGE)
		block(ModBlocks.GLOOMROCK_SMOOTH_YELLOW)
		block(ModBlocks.GLOOMROCK_SMOOTH_GREEN)
		block(ModBlocks.GLOOMROCK_SMOOTH_CYAN)
		block(ModBlocks.GLOOMROCK_SMOOTH_BLUE)
		block(ModBlocks.GLOOMROCK_SMOOTH_PURPLE)
		block(ModBlocks.GLOOMROCK_SMOOTH_MAGENTA)
		block(ModBlocks.GLOOMROCK_SMOOTH_WHITE)
		simple(ModBlocks.GLOOMTORCH.asItem())
		
		// Blocks: Building (Dusty Stone)
		
		block(ModBlocks.DUSTY_STONE)
		block(ModBlocks.DUSTY_STONE_CRACKED)
		block(ModBlocks.DUSTY_STONE_DAMAGED)
		block(ModBlocks.DUSTY_STONE_BRICKS)
		block(ModBlocks.DUSTY_STONE_CRACKED_BRICKS)
		block(ModBlocks.DUSTY_STONE_DECORATION)
		block(ModBlocks.DUSTY_STONE_BRICK_STAIRS)
		block(ModBlocks.DUSTY_STONE_BRICK_SLAB)
		
		// Blocks: Building (Obsidian)
		
		block(ModBlocks.OBSIDIAN_STAIRS)
		block(ModBlocks.OBSIDIAN_FALLING)
		block(ModBlocks.OBSIDIAN_SMOOTH)
		block(ModBlocks.OBSIDIAN_CHISELED)
		block(ModBlocks.OBSIDIAN_PILLAR)
		block(ModBlocks.OBSIDIAN_SMOOTH_LIT, ModBlocks.OBSIDIAN_SMOOTH)
		block(ModBlocks.OBSIDIAN_CHISELED_LIT, ModBlocks.OBSIDIAN_CHISELED)
		block(ModBlocks.OBSIDIAN_PILLAR_LIT, ModBlocks.OBSIDIAN_PILLAR)
		block(ModBlocks.OBSIDIAN_TOWER_TOP, ModBlocks.OBSIDIAN_CHISELED)
		
		// Blocks: Building (End Stone)
		
		block(ModBlocks.END_STONE_INFESTED)
		block(ModBlocks.END_STONE_BURNED)
		block(ModBlocks.END_STONE_ENCHANTED)
		
		// Blocks: Building (Dark Loam)
		
		block(ModBlocks.DARK_LOAM)
		block(ModBlocks.DARK_LOAM_SLAB)
		
		// Blocks: Building (Grave Dirt)
		
		parent(ModBlocks.GRAVE_DIRT_PLAIN, Resource.Custom("block/grave_dirt_low"))
		parent(ModBlocks.GRAVE_DIRT_LOOT, Resource.Custom("block/grave_dirt_loot_4"), checkExistence = false)
		parent(ModBlocks.GRAVE_DIRT_SPIDERLING, Resource.Custom("block/grave_dirt_low"))
		
		// Blocks: Building (Wood)
		
		block(ModBlocks.WHITEBARK_LOG)
		block(ModBlocks.WHITEBARK)
		block(ModBlocks.WHITEBARK_PLANKS)
		block(ModBlocks.WHITEBARK_STAIRS)
		block(ModBlocks.WHITEBARK_SLAB)
		
		// Blocks: Building (Miner's Burial)
		
		block(ModBlocks.MINERS_BURIAL_BLOCK_PLAIN)
		block(ModBlocks.MINERS_BURIAL_BLOCK_CHISELED)
		block(ModBlocks.MINERS_BURIAL_BLOCK_PILLAR)
		block(ModBlocks.MINERS_BURIAL_BLOCK_JAIL)
		block(ModBlocks.MINERS_BURIAL_ALTAR)
		
		// Blocks: Interactive (Storage)
		
		parent(ModBlocks.DARK_CHEST, Blocks.CHEST.asItem().r)
		parent(ModBlocks.LOOT_CHEST, Blocks.CHEST.asItem().r)
		
		// Blocks: Interactive (Puzzle)
		
		block(ModBlocks.PUZZLE_WALL)
		parent(ModBlocks.PUZZLE_PLAIN, Resource.Custom("block/puzzle_base_active"), checkExistence = false)
		parent(ModBlocks.PUZZLE_BURST_3, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_burst_3"))
		}
		parent(ModBlocks.PUZZLE_BURST_5, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_burst_5"))
		}
		parent(ModBlocks.PUZZLE_REDIRECT_1, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_redirect_1n"))
		}
		parent(ModBlocks.PUZZLE_REDIRECT_2, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_redirect_2ns"))
		}
		parent(ModBlocks.PUZZLE_REDIRECT_4, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_redirect_4"))
		}
		parent(ModBlocks.PUZZLE_TELEPORT, Resource.Custom("block/puzzle_block_inventory")).then {
			texture("top", Resource.Custom("block/puzzle_overlay_teleport"))
		}
		
		// Blocks: Interactive (Gates)
		
		block(ModBlocks.EXPERIENCE_GATE)
		block(ModBlocks.EXPERIENCE_GATE_CONTROLLER)
		
		// Blocks: Interactive (Uncategorized)
		
		block(ModBlocks.INFUSED_TNT)
		simple(ModBlocks.IGNEOUS_PLATE)
		simple(ModBlocks.ENHANCED_BREWING_STAND.asItem())
		
		// Blocks: Ores
		
		block(ModBlocks.END_POWDER_ORE)
		block(ModBlocks.ENDIUM_ORE)
		block(ModBlocks.IGNEOUS_ROCK_ORE)
		
		// Blocks: Decorative (Trees)
		
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_RED)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		block(ModBlocks.WHITEBARK_LEAVES_AUTUMN_RED)
		block(ModBlocks.WHITEBARK_LEAVES_AUTUMN_BROWN)
		block(ModBlocks.WHITEBARK_LEAVES_AUTUMN_ORANGE)
		block(ModBlocks.WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN)
		
		// Blocks: Decorative (Plants)
		
		simple(ModBlocks.DEATH_FLOWER_DECAYING, ModBlocks.DEATH_FLOWER_DECAYING.r("_1")).then {
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_2")){ predicate(Resource.Custom("death_level"), 4F) }
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_3")){ predicate(Resource.Custom("death_level"), 8F) }
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_4")){ predicate(Resource.Custom("death_level"), 12F) }
		}
		multi(ModBlocks.DEATH_FLOWER_DECAYING, Resource.Vanilla("item/generated"), 1..4){
			texture("layer0", Resource.Custom("block/" + it.path))
		}
		simple(ModBlocks.DEATH_FLOWER_HEALED)
		simple(ModBlocks.DEATH_FLOWER_WITHERED)
		
		// Blocks: Decorative (Uncategorized)
		
		simple(ModBlocks.ANCIENT_COBWEB)
		simple(ModBlocks.DRY_VINES, Blocks.VINE.r)
		block(ModBlocks.ENDERMAN_HEAD)
		
		// Blocks: Portals
		
		block(ModBlocks.END_PORTAL_FRAME)
		block(ModBlocks.END_PORTAL_ACCEPTOR)
		block(ModBlocks.VOID_PORTAL_FRAME)
		block(ModBlocks.VOID_PORTAL_STORAGE)
		block(ModBlocks.VOID_PORTAL_FRAME_CRAFTED)
		block(ModBlocks.VOID_PORTAL_STORAGE_CRAFTED)
		
		// Blocks: Energy
		
		simple(ModBlocks.ENERGY_CLUSTER)
		block(ModBlocks.CORRUPTED_ENERGY)
		
		// Blocks: Tables
		
		block(ModBlocks.TABLE_PEDESTAL)
		block(ModBlocks.TABLE_BASE_TIER_1)
		block(ModBlocks.TABLE_BASE_TIER_2)
		block(ModBlocks.TABLE_BASE_TIER_3)
		block(ModBlocks.ACCUMULATION_TABLE_TIER_1)
		block(ModBlocks.ACCUMULATION_TABLE_TIER_2)
		block(ModBlocks.ACCUMULATION_TABLE_TIER_3)
		block(ModBlocks.EXPERIENCE_TABLE_TIER_1)
		block(ModBlocks.EXPERIENCE_TABLE_TIER_2)
		block(ModBlocks.EXPERIENCE_TABLE_TIER_3)
		block(ModBlocks.INFUSION_TABLE_TIER_1)
		block(ModBlocks.INFUSION_TABLE_TIER_2)
		block(ModBlocks.INFUSION_TABLE_TIER_3)
		
		// Blocks: Utilities
		
		block(ModBlocks.SCAFFOLDING)
	}
}
