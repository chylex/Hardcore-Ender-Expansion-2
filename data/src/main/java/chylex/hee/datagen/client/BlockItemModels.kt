package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.block
import chylex.hee.datagen.client.util.multi
import chylex.hee.datagen.client.util.override
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.r
import chylex.hee.datagen.then
import chylex.hee.game.Resource
import chylex.hee.init.ModBlocks
import net.minecraft.block.Blocks
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class BlockItemModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : ItemModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		
		// Blocks: Building (Uncategorized)
		
		parent(ModBlocks.STONE_BRICK_WALL, Resource.Custom("block/stone_brick_wall_inventory"), checkExistence = false)
		parent(ModBlocks.INFUSED_GLASS, Resource.Custom("block/infused_glass_c0"))
		block(ModBlocks.ENDERSOL)
		block(ModBlocks.HUMUS)
		
		// Blocks: Building (Gloomrock)
		
		simple(ModBlocks.GLOOMTORCH.asItem())
		
		// Blocks: Building (Grave Dirt)
		
		parent(ModBlocks.GRAVE_DIRT_PLAIN, Resource.Custom("block/grave_dirt_low"))
		parent(ModBlocks.GRAVE_DIRT_LOOT, Resource.Custom("block/grave_dirt_loot_4"), checkExistence = false)
		parent(ModBlocks.GRAVE_DIRT_SPIDERLING, Resource.Custom("block/grave_dirt_low"))
		
		// Blocks: Interactive (Storage)
		
		parent(ModBlocks.DARK_CHEST, Blocks.CHEST.asItem().r)
		parent(ModBlocks.LOOT_CHEST, Blocks.CHEST.asItem().r)
		
		// Blocks: Interactive (Puzzle)
		
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
		
		// Blocks: Interactive (Uncategorized)
		
		simple(ModBlocks.IGNEOUS_PLATE)
		simple(ModBlocks.ENHANCED_BREWING_STAND.asItem())
		
		// Blocks: Decorative (Trees)
		
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_RED)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE)
		simple(ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
		
		// Blocks: Decorative (Plants)
		
		simple(ModBlocks.DEATH_FLOWER_DECAYING, ModBlocks.DEATH_FLOWER_DECAYING.r("_1")).then {
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_2")) { predicate(Resource.Custom("death_level"), 4F) }
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_3")) { predicate(Resource.Custom("death_level"), 8F) }
			override(ModBlocks.DEATH_FLOWER_DECAYING.asItem().r("_4")) { predicate(Resource.Custom("death_level"), 12F) }
		}
		
		multi(ModBlocks.DEATH_FLOWER_DECAYING, Resource.Vanilla("item/generated"), 1..4) {
			texture("layer0", Resource.Custom("block/" + it.path))
		}
		
		simple(ModBlocks.DEATH_FLOWER_HEALED)
		simple(ModBlocks.DEATH_FLOWER_WITHERED)
		
		// Blocks: Decorative (Uncategorized)
		
		simple(ModBlocks.ANCIENT_COBWEB)
		simple(ModBlocks.DRY_VINES, Blocks.VINE.r)
		block(ModBlocks.ENDERMAN_HEAD)
		
		// Blocks: Energy
		
		simple(ModBlocks.ENERGY_CLUSTER)
		
		// Blocks: Tables
		
		block(ModBlocks.TABLE_PEDESTAL)
	}
}
