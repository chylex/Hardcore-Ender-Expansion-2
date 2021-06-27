package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.layers
import chylex.hee.datagen.client.util.multi
import chylex.hee.datagen.client.util.override
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.client.util.suffixed
import chylex.hee.datagen.r
import chylex.hee.datagen.then
import chylex.hee.init.ModItems
import chylex.hee.system.facades.Resource
import net.minecraft.data.DataGenerator
import net.minecraft.item.Items
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class ItemModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : ItemModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		
		// Items: Raw Resources
		
		simple(ModItems.ETHEREUM)
		simple(ModItems.ANCIENT_DUST)
		simple(ModItems.END_POWDER)
		simple(ModItems.STARDUST)
		simple(ModItems.ENDIUM_INGOT)
		simple(ModItems.ENDIUM_NUGGET)
		simple(ModItems.OBSIDIAN_FRAGMENT)
		simple(ModItems.IGNEOUS_ROCK)
		simple(ModItems.PUZZLE_MEDALLION)
		simple(ModItems.INFERNIUM)
		simple(ModItems.INFERNIUM_INGOT)
		simple(ModItems.AURICION)
		simple(ModItems.DRAGON_SCALE)
		simple(ModItems.INSTABILITY_ORB)
		simple(ModItems.ECTOPLASM)
		simple(ModItems.ENCHANTED_CLAW)
		
		// Items: Manufactured Resources
		
		simple(ModItems.ALTERATION_NEXUS)
		simple(ModItems.VOID_ESSENCE)
		simple(ModItems.OBSIDIAN_ROD)
		simple(ModItems.PURITY_EXTRACT)
		simple(ModItems.STATIC_CORE)
		simple(ModItems.TICKING_CORE)
		simple(ModItems.DIRTY_INFERNIUM_INGOT)
		simple(ModItems.AMELIOR)
		simple(ModItems.REVITALIZATION_SUBSTANCE)
		layers(ModItems.BINDING_ESSENCE, arrayOf("binding_essence_primary", "binding_essence_secondary", "binding_essence_tertiary", "binding_essence_quaternary"))
		
		// Items: Nature & Food
		
		simple(ModItems.COMPOST)
		
		simple(ModItems.VOID_SALAD).then {
			override(Resource.Custom("item/void_void_salad")) { predicate(Resource.Custom("void_salad_type"), 1F) }
			override(Resource.Custom("item/mega_void_salad")) { predicate(Resource.Custom("void_salad_type"), 2F) }
		}
		
		simple("void_void_salad")
		simple("mega_void_salad")
		
		// Items: Table Cores
		
		simple(ModItems.ACCUMULATION_TABLE_CORE)
		simple(ModItems.EXPERIENCE_TABLE_CORE)
		simple(ModItems.INFUSION_TABLE_CORE)
		
		// Items: Utilities
		
		simple(ModItems.TABLE_LINK)
		simple(ModItems.KNOWLEDGE_NOTE)
		parent(ModItems.ENDERMAN_HEAD, Resource.Vanilla("item/template_skull"))
		simple(ModItems.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE.r)
		
		// Items: Tools
		
		simple(ModItems.VOID_MINER)
		
		simple(ModItems.VOID_BUCKET).then {
			override(ModItems.VOID_BUCKET.r("_fluid_level_1")) { predicate(Resource.Custom("void_bucket_cooldown"), 0.01F) }
			override(ModItems.VOID_BUCKET.r("_fluid_level_2")) { predicate(Resource.Custom("void_bucket_cooldown"), 0.3F) }
			override(ModItems.VOID_BUCKET.r("_fluid_level_3")) { predicate(Resource.Custom("void_bucket_cooldown"), 0.5F) }
			override(ModItems.VOID_BUCKET.r("_fluid_level_4")) { predicate(Resource.Custom("void_bucket_cooldown"), 0.7F) }
		}
		
		multi(ModItems.VOID_BUCKET, Resource.Vanilla("item/generated"), Array(4) { "_fluid_level_${it + 1}" }) {
			texture("layer0", Resource.Custom("item/void_bucket"))
			texture("layer1", Resource.Custom("item/$it"))
		}
		
		simple(ModItems.SCORCHING_PICKAXE)
		simple(ModItems.SCORCHING_SHOVEL)
		simple(ModItems.SCORCHING_AXE)
		simple(ModItems.SCORCHING_SWORD)
		simple(ModItems.FLINT_AND_INFERNIUM)
		
		// Items: Fluids
		
		simple(ModItems.ENDER_GOO_BUCKET)
		simple(ModItems.PURIFIED_ENDER_GOO_BUCKET)
		
		// Items: Energy
		
		layers(ModItems.ENERGY_ORACLE, arrayOf("energy_oracle", "energy_oracle_indicator_inactive")).then {
			override(ModItems.ENERGY_ORACLE.r("_active_mild")) { predicate(Resource.Custom("activity_intensity"), 0.5F) }
			override(ModItems.ENERGY_ORACLE.r("_active_full")) { predicate(Resource.Custom("activity_intensity"), 1F) }
		}
		
		multi(ModItems.ENERGY_ORACLE, Resource.Vanilla("item/generated"), arrayOf("_active_mild", "_active_full")) {
			texture("layer0", Resource.Custom("item/energy_oracle"))
			texture("layer1", Resource.Custom("item/energy_oracle_indicator" + it.suffix))
		}
		
		simple(ModItems.ENERGY_RECEPTACLE).then {
			override(ModItems.ENERGY_RECEPTACLE.r("_with_cluster")) { predicate(Resource.Custom("has_cluster"), 1F) }
		}
		
		layers(ModItems.ENERGY_RECEPTACLE.suffixed("_with_cluster"), arrayOf("energy_receptacle", "energy_receptacle_cluster"))
		
		// Items: Gems & Teleportation
		
		simple(ModItems.INFUSED_ENDER_PEARL, Items.ENDER_PEARL.r)
		simple(ModItems.SPATIAL_DASH_GEM)
		simple(ModItems.LINKING_GEM)
		
		layers(ModItems.PORTAL_TOKEN, arrayOf("portal_token_outline", "portal_token_color_top", "portal_token_color_bottom")).then {
			override(ModItems.PORTAL_TOKEN.r("_rare")) { predicate(Resource.Custom("token_type"), 1F) }
			override(ModItems.PORTAL_TOKEN.r("_rare_corrupted")) { predicate(Resource.Custom("token_type"), 1.5F) }
			override(ModItems.PORTAL_TOKEN.r("_solitary")) { predicate(Resource.Custom("token_type"), 2F) }
		}
		
		layers(ModItems.PORTAL_TOKEN.suffixed("_rare"), arrayOf("portal_token_outline", "portal_token_color_top", "portal_token_color_bottom", "portal_token_border_rare"))
		layers(ModItems.PORTAL_TOKEN.suffixed("_rare_corrupted"), arrayOf("portal_token_outline", "portal_token_color_top", "portal_token_color_bottom", "portal_token_border_rare", "portal_token_corruption"))
		layers(ModItems.PORTAL_TOKEN.suffixed("_solitary"), arrayOf("portal_token_outline", "portal_token_color_top", "portal_token_color_bottom", "portal_token_border_solitary"))
		simple(ModItems.BLANK_TOKEN)
		
		// Items: Trinkets
		
		simple(ModItems.TRINKET_POUCH)
		
		simple(ModItems.TOTEM_OF_UNDYING).then {
			override(ModItems.TOTEM_OF_UNDYING.r("_shaking")) { predicate(Resource.Custom("is_shaking"), 1F) }
		}
		
		simple(ModItems.TOTEM_OF_UNDYING.suffixed("_shaking"))
		simple(ModItems.AMULET_OF_RECOVERY)
		simple(ModItems.AMULET_OF_RECOVERY.suffixed("_held"))
		simple(ModItems.RING_OF_HUNGER)
		simple(ModItems.RING_OF_PRESERVATION)
		simple(ModItems.TALISMAN_OF_GRIEFING)
		simple(ModItems.SCALE_OF_FREEFALL)
		
		// Items: Spawn Eggs
		
		parent(ModItems.SPAWN_ENDER_EYE, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_ANGRY_ENDERMAN, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_BLOBBY, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_ENDERMITE_INSTABILITY, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_SPIDERLING, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_UNDREAD, Resource.Vanilla("item/template_spawn_egg"))
		parent(ModItems.SPAWN_VAMPIRE_BAT, Resource.Vanilla("item/template_spawn_egg"))
	}
}
