package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.cauldron
import chylex.hee.datagen.client.util.cross
import chylex.hee.datagen.client.util.cube
import chylex.hee.datagen.client.util.cubeBottomTop
import chylex.hee.datagen.client.util.cubeColumn
import chylex.hee.datagen.client.util.flowerPot
import chylex.hee.datagen.client.util.leaves
import chylex.hee.datagen.client.util.multi
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.particle
import chylex.hee.datagen.client.util.portalFrame
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.client.util.suffixed
import chylex.hee.datagen.client.util.table
import chylex.hee.datagen.client.util.wall
import chylex.hee.datagen.r
import chylex.hee.datagen.then
import chylex.hee.game.Resource
import chylex.hee.init.ModBlocks
import net.minecraft.block.Blocks
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.BlockModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class BlockModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : BlockModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		
		// Blocks: Building (Uncategorized)
		
		wall(ModBlocks.STONE_BRICK_WALL, Blocks.STONE_BRICKS.r)
		simple(ModBlocks.STONE_BRICK_WALL.suffixed("_inventory"), Resource.Vanilla("block/wall_inventory"), "wall", Blocks.STONE_BRICKS.r)
		cubeColumn(ModBlocks.ENDERSOL)
		cubeBottomTop(ModBlocks.ENDERSOL.suffixed("_merge_1"), ModBlocks.ENDERSOL.r("_merge_1"), Blocks.END_STONE.r, ModBlocks.ENDERSOL.r("_top"))
		cubeBottomTop(ModBlocks.ENDERSOL.suffixed("_merge_2"), ModBlocks.ENDERSOL.r("_merge_2"), Blocks.END_STONE.r, ModBlocks.ENDERSOL.r("_top"))
		cube(ModBlocks.HUMUS)
		cubeBottomTop(ModBlocks.HUMUS.suffixed("_merge"), ModBlocks.HUMUS.r("_merge"), ModBlocks.ENDERSOL.r("_top"), ModBlocks.HUMUS.r)
		
		// Blocks: Building (Obsidian)
		
		cube(ModBlocks.OBSIDIAN_FALLING, Blocks.OBSIDIAN.r)
		
		// Blocks: Building (End Stone)
		
		cubeBottomTop(ModBlocks.END_STONE_INFESTED, bottom = Blocks.END_STONE.r).then {
			texture("particle", ModBlocks.END_STONE_INFESTED.r("_top"))
		}
		
		cubeBottomTop(ModBlocks.END_STONE_BURNED, bottom = Blocks.END_STONE.r).then {
			texture("particle", ModBlocks.END_STONE_BURNED.r("_top"))
		}
		
		cubeBottomTop(ModBlocks.END_STONE_ENCHANTED, bottom = Blocks.END_STONE.r).then {
			texture("particle", ModBlocks.END_STONE_ENCHANTED.r("_top"))
		}
		
		// Blocks: Building (Grave Dirt)
		
		cube(ModBlocks.GRAVE_DIRT_PLAIN.suffixed("_full"), ModBlocks.GRAVE_DIRT_PLAIN.r).then {
			texture("particle", ModBlocks.GRAVE_DIRT_PLAIN.r)
		}
		
		multi(ModBlocks.GRAVE_DIRT_LOOT, Resource.Custom("block/grave_dirt_low"), 1..6) {
			texture("top", Resource.Custom("block/$it"))
		}
		
		// Blocks: Building (Wood)
		
		cube(ModBlocks.WHITEBARK, ModBlocks.WHITEBARK_LOG.r)
		
		// Blocks: Fluids
		
		particle(ModBlocks.ENDER_GOO, ModBlocks.ENDER_GOO.r("_still"))
		particle(ModBlocks.PURIFIED_ENDER_GOO, ModBlocks.PURIFIED_ENDER_GOO.r("_still"))
		cauldron(ModBlocks.CAULDRON_ENDER_GOO, ModBlocks.ENDER_GOO.r("_still"))
		cauldron(ModBlocks.CAULDRON_PURIFIED_ENDER_GOO, ModBlocks.PURIFIED_ENDER_GOO.r("_still"))
		cauldron(ModBlocks.CAULDRON_DRAGONS_BREATH, Resource.Custom("block/dragons_breath_still"))
		
		// Blocks: Interactive (Storage)
		
		particle(ModBlocks.DARK_CHEST, ModBlocks.GLOOMROCK_SMOOTH.r)
		
		parent(ModBlocks.LOOT_CHEST, Resource.Vanilla("block/block")).then {
			texture("particle", ModBlocks.LOOT_CHEST.r("_particle"))
		}
		
		// Blocks: Interactive (Puzzle)
		
		arrayOf("active", "disabled", "inactive").forEach {
			parent("puzzle_base_$it", Resource.Vanilla("block/cube_all")).then {
				texture("all", Resource.Custom("block/puzzle_base_$it"))
			}
		}
		
		arrayOf("burst_3", "burst_5", "redirect_1e", "redirect_1n", "redirect_1s", "redirect_1w", "redirect_2ew", "redirect_2ns", "redirect_4", "teleport").forEach {
			parent("puzzle_overlay_$it", Resource.Custom("block/puzzle_overlay")).then {
				texture("overlay", Resource.Custom("block/puzzle_overlay_$it"))
			}
		}
		
		// Blocks: Interactive (Gates)
		
		cubeBottomTop(ModBlocks.EXPERIENCE_GATE, top = ModBlocks.EXPERIENCE_GATE.r("_bottom"))
		
		multi(ModBlocks.EXPERIENCE_GATE, ModBlocks.EXPERIENCE_GATE.r, arrayOf("_rd1", "_rd2", "_ud")) {
			texture("top", Resource.Custom("block/experience_gate_top" + it.suffix))
		}
		
		cubeBottomTop(ModBlocks.EXPERIENCE_GATE_CONTROLLER, ModBlocks.EXPERIENCE_GATE.r("_side"), ModBlocks.EXPERIENCE_GATE.r("_bottom"), ModBlocks.EXPERIENCE_GATE.r("_top_controller"))
		
		// Blocks: Interactive (Uncategorized)
		
		cubeBottomTop(ModBlocks.INFUSED_TNT, Blocks.TNT.r("_side"), Blocks.TNT.r("_bottom"), Blocks.TNT.r("_top"))
		particle(ModBlocks.IGNEOUS_PLATE, ModBlocks.IGNEOUS_PLATE.r)
		
		parent(ModBlocks.ENHANCED_BREWING_STAND, Blocks.BREWING_STAND.r).then {
			texture("particle", Blocks.BREWING_STAND.r)
			texture("base", Blocks.BREWING_STAND.r("_base"))
			texture("stand", ModBlocks.ENHANCED_BREWING_STAND.r)
		}
		
		// Blocks: Ores
		
		parent(ModBlocks.STARDUST_ORE, Resource.Custom("block/cube_overlay")).then {
			texture("particle", ModBlocks.STARDUST_ORE.r("_particle"))
			texture("base", Blocks.END_STONE.r)
		}
		
		// Blocks: Decorative (Trees)
		
		cross(ModBlocks.WHITEBARK_SAPLING_AUTUMN_RED)
		cross(ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN)
		cross(ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE)
		cross(ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		leaves(ModBlocks.WHITEBARK_LEAVES_AUTUMN_RED)
		leaves(ModBlocks.WHITEBARK_LEAVES_AUTUMN_BROWN)
		leaves(ModBlocks.WHITEBARK_LEAVES_AUTUMN_ORANGE)
		leaves(ModBlocks.WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN)
		flowerPot(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_RED, ModBlocks.WHITEBARK_SAPLING_AUTUMN_RED)
		flowerPot(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_BROWN, ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN)
		flowerPot(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_ORANGE, ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE)
		flowerPot(ModBlocks.POTTED_WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN, ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		
		// Blocks: Decorative (Plants)
		
		multi(ModBlocks.DEATH_FLOWER_DECAYING, Resource.Vanilla("block/cross"), 1..4) {
			texture("cross", Resource.Custom("block/$it"))
		}
		
		cross(ModBlocks.DEATH_FLOWER_HEALED)
		cross(ModBlocks.DEATH_FLOWER_WITHERED)
		
		multi(ModBlocks.POTTED_DEATH_FLOWER_DECAYING, Resource.Vanilla("block/flower_pot_cross"), 1..4) {
			texture("plant", Resource.Custom("block/death_flower" + it.suffix))
		}
		
		flowerPot(ModBlocks.POTTED_DEATH_FLOWER_HEALED, ModBlocks.DEATH_FLOWER_HEALED)
		flowerPot(ModBlocks.POTTED_DEATH_FLOWER_WITHERED, ModBlocks.DEATH_FLOWER_WITHERED)
		
		// Blocks: Decorative (Uncategorized)
		
		cross(ModBlocks.ANCIENT_COBWEB)
		
		// Blocks: Portals
		
		portalFrame(ModBlocks.END_PORTAL_FRAME, ModBlocks.END_PORTAL_FRAME.r("_side"), ModBlocks.END_PORTAL_FRAME.r("_top_plain"))
		portalFrame(ModBlocks.END_PORTAL_ACCEPTOR, ModBlocks.END_PORTAL_FRAME.r("_side"), ModBlocks.END_PORTAL_FRAME.r("_top_acceptor"))
		portalFrame(ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_FRAME.r("_side"), ModBlocks.VOID_PORTAL_FRAME.r("_top_plain"))
		portalFrame(ModBlocks.VOID_PORTAL_STORAGE, ModBlocks.VOID_PORTAL_FRAME.r("_side"), ModBlocks.VOID_PORTAL_FRAME.r("_top_storage"))
		portalFrame(ModBlocks.VOID_PORTAL_FRAME_CRAFTED, ModBlocks.VOID_PORTAL_FRAME.r("_side"), ModBlocks.VOID_PORTAL_FRAME.r("_top_plain"))
		portalFrame(ModBlocks.VOID_PORTAL_STORAGE_CRAFTED, ModBlocks.VOID_PORTAL_FRAME.r("_side"), ModBlocks.VOID_PORTAL_FRAME.r("_top_storage"))
		
		// Blocks: Energy
		
		cross(ModBlocks.CORRUPTED_ENERGY, Blocks.BARRIER.asItem().r).then { ao(false) }
		
		// Blocks: Tables
		
		for (tier in 1..3) {
			parent("table_tier_$tier", Resource.Custom("block/table")).then {
				texture("particle", "hee:block/table_base")
				texture("bottom", "hee:block/table_base")
				texture("top", "hee:block/table_base")
				texture("side", "hee:block/table_base_side_$tier")
			}
		}
		
		parent(ModBlocks.TABLE_BASE_TIER_1, Resource.Custom("block/table_tier_1")).then {
			Resource.Custom("block/transparent").let {
				texture("overlay_top", it)
				texture("overlay_side", it)
			}
		}
		
		parent(ModBlocks.TABLE_BASE_TIER_2, Resource.Custom("block/table_tier_2")).then {
			Resource.Custom("block/transparent").let {
				texture("overlay_top", it)
				texture("overlay_side", it)
			}
		}
		
		parent(ModBlocks.TABLE_BASE_TIER_3, Resource.Custom("block/table_tier_3")).then {
			Resource.Custom("block/transparent").let {
				texture("overlay_top", it)
				texture("overlay_side", it)
			}
		}
		
		table(ModBlocks.ACCUMULATION_TABLE_TIER_1)
		table(ModBlocks.ACCUMULATION_TABLE_TIER_2)
		table(ModBlocks.ACCUMULATION_TABLE_TIER_3)
		table(ModBlocks.EXPERIENCE_TABLE_TIER_1)
		table(ModBlocks.EXPERIENCE_TABLE_TIER_2)
		table(ModBlocks.EXPERIENCE_TABLE_TIER_3)
		table(ModBlocks.INFUSION_TABLE_TIER_1)
		table(ModBlocks.INFUSION_TABLE_TIER_2)
		table(ModBlocks.INFUSION_TABLE_TIER_3)
	}
}
