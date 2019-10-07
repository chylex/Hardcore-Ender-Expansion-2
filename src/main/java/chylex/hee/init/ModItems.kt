package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.dispenser.DispenseEndermanHead
import chylex.hee.game.block.dispenser.DispenseWaterExtinguishIgneousPlate
import chylex.hee.game.item.ItemAmuletOfRecovery
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemBucketWithCauldron
import chylex.hee.game.item.ItemChorusBerry
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemElytraOverride
import chylex.hee.game.item.ItemEndPowder
import chylex.hee.game.item.ItemEndermanHead
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemEyeOfEnderOverride
import chylex.hee.game.item.ItemFlintAndInfernium
import chylex.hee.game.item.ItemIgneousRock
import chylex.hee.game.item.ItemInfusedEnderPearl
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemPurifiedEnderGooBucket
import chylex.hee.game.item.ItemRevitalizationSubstance
import chylex.hee.game.item.ItemRingOfHunger
import chylex.hee.game.item.ItemRingOfPreservation
import chylex.hee.game.item.ItemScaleOfFreefall
import chylex.hee.game.item.ItemScorchingSword
import chylex.hee.game.item.ItemScorchingTool
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.ItemSpatialDashGem
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.item.ItemTalismanOfGriefing
import chylex.hee.game.item.ItemTotemOfUndyingCustom
import chylex.hee.game.item.ItemTotemOfUndyingOverride
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidMiner
import chylex.hee.game.item.ItemVoidSalad
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.useVanillaName
import net.minecraft.block.BlockDispenser
import net.minecraft.block.BlockShulkerBox
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent

@SubscribeAllEvents(modid = HEE.ID)
object ModItems{
	
	// Items: Raw resources
	
	@JvmField val ETHEREUM          = Item().apply { setup("ethereum") }
	@JvmField val ANCIENT_DUST      = Item().apply { setup("ancient_dust") }
	@JvmField val END_POWDER        = ItemEndPowder().apply { setup("end_powder") }
	@JvmField val STARDUST          = Item().apply { setup("stardust") }
	@JvmField val ENDIUM_INGOT      = Item().apply { setup("endium_ingot") }
	@JvmField val ENDIUM_NUGGET     = Item().apply { setup("endium_nugget") }
	@JvmField val OBSIDIAN_FRAGMENT = Item().apply { setup("obsidian_fragment") }
	@JvmField val IGNEOUS_ROCK      = ItemIgneousRock().apply { setup("igneous_rock") }
	@JvmField val PUZZLE_MEDALLION  = Item().apply { setup("puzzle_medallion") }
	@JvmField val INFERNIUM         = Item().apply { setup("infernium") }
	@JvmField val INFERNIUM_INGOT   = Item().apply { setup("infernium_ingot") }
	@JvmField val AURICION          = Item().apply { setup("auricion") }
	@JvmField val DRAGON_SCALE      = Item().apply { setup("dragon_scale") }
	
	// Items: Manufactured resources
	
	@JvmField val ALTERATION_NEXUS         = Item().apply { setup("alteration_nexus") }
	@JvmField val VOID_ESSENCE             = Item().apply { setup("void_essence") }
	@JvmField val OBSIDIAN_ROD             = Item().apply { setup("obsidian_rod") }
	@JvmField val PURITY_EXTRACT           = Item().apply { setup("purity_extract") }
	@JvmField val STATIC_CORE              = Item().apply { setup("static_core") }
	@JvmField val DIRTY_INFERNIUM_INGOT    = Item().apply { setup("dirty_infernium_ingot") }
	@JvmField val REVITALIZATION_SUBSTANCE = ItemRevitalizationSubstance().apply { setup("revitalization_substance") }
	@JvmField val BINDING_ESSENCE          = ItemBindingEssence().apply { setup("binding_essence") }
	
	// Items: Nature & food
	
	@JvmField val COMPOST    = ItemCompost().apply { setup("compost") }
	@JvmField val VOID_SALAD = ItemVoidSalad().apply { setup("void_salad") }
	
	// Items: Utilities
	
	@JvmField val TABLE_LINK     = ItemTableLink().apply { setup("table_link") }
	@JvmField val KNOWLEDGE_NOTE = Item().apply { setup("knowledge_note") } // TODO
	@JvmField val ENDERMAN_HEAD  = ItemEndermanHead().apply { setup("enderman_head") }
	
	// Items: Tools
	
	@JvmField val VOID_MINER          = ItemVoidMiner().apply { setup("void_miner") }
	@JvmField val VOID_BUCKET         = ItemVoidBucket().apply { setup("void_bucket") }
	@JvmField val SCORCHING_PICKAXE   = ItemScorchingTool(PICKAXE).apply { setup("scorching_pickaxe") }
	@JvmField val SCORCHING_SHOVEL    = ItemScorchingTool(SHOVEL).apply { setup("scorching_shovel") }
	@JvmField val SCORCHING_AXE       = ItemScorchingTool(AXE).apply { setup("scorching_axe") }
	@JvmField val SCORCHING_SWORD     = ItemScorchingSword().apply { setup("scorching_sword") }
	@JvmField val FLINT_AND_INFERNIUM = ItemFlintAndInfernium().apply { setup("flint_and_infernium") }
	
	// Items: Fluids
	
	@JvmField val ENDER_GOO_BUCKET          = ItemBucketWithCauldron(ModBlocks.ENDER_GOO, ModBlocks.CAULDRON_ENDER_GOO).apply { setup("ender_goo_bucket"); containerItem = Items.BUCKET }
	@JvmField val PURIFIED_ENDER_GOO_BUCKET = ItemPurifiedEnderGooBucket().apply { setup("purified_ender_goo_bucket"); containerItem = Items.BUCKET }
	
	// Items: Energy
	
	@JvmField val ENERGY_ORACLE     = ItemEnergyOracle().apply { setup("energy_oracle") }
	@JvmField val ENERGY_RECEPTACLE = ItemEnergyReceptacle().apply { setup("energy_receptacle") }
	
	// Items: Gems & teleportation
	
	@JvmField val INFUSED_ENDER_PEARL = ItemInfusedEnderPearl().apply { setup("infused_ender_pearl", translationKey = "enderPearl", inCreativeTab = false) }
	@JvmField val SPATIAL_DASH_GEM    = ItemSpatialDashGem().apply { setup("spatial_dash_gem") }
	@JvmField val PORTAL_TOKEN        = ItemPortalToken().apply { setup("portal_token") }
	@JvmField val BLANK_TOKEN         = Item().apply { setup("blank_token"); setMaxStackSize(ItemPortalToken.MAX_STACK_SIZE) }
	
	// Items: Trinkets
	
	@JvmField val TRINKET_POUCH        = ItemTrinketPouch().apply { setup("trinket_pouch") }
	@JvmField val TOTEM_OF_UNDYING     = ItemTotemOfUndyingCustom().apply { setup("totem_of_undying", translationKey = "totem") }
	@JvmField val AMULET_OF_RECOVERY   = ItemAmuletOfRecovery().apply { setup("amulet_of_recovery") }
	@JvmField val RING_OF_HUNGER       = ItemRingOfHunger().apply { setup("ring_of_hunger") }
	@JvmField val RING_OF_PRESERVATION = ItemRingOfPreservation().apply { setup("ring_of_preservation") }
	@JvmField val TALISMAN_OF_GRIEFING = ItemTalismanOfGriefing().apply { setup("talisman_of_griefing") }
	@JvmField val SCALE_OF_FREEFALL    = ItemScaleOfFreefall().apply { setup("scale_of_freefall") }
	
	// Registry
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Item>){
		with(e.registry){
			register(ETHEREUM)
			register(ANCIENT_DUST)
			register(END_POWDER)
			register(STARDUST)
			register(ENDIUM_INGOT)
			register(ENDIUM_NUGGET)
			register(OBSIDIAN_FRAGMENT)
			register(IGNEOUS_ROCK)
			register(PUZZLE_MEDALLION)
			register(INFERNIUM)
			register(INFERNIUM_INGOT)
			register(AURICION)
			register(DRAGON_SCALE)
			
			register(ALTERATION_NEXUS)
			register(VOID_ESSENCE)
			register(OBSIDIAN_ROD)
			register(PURITY_EXTRACT)
			register(STATIC_CORE)
			register(DIRTY_INFERNIUM_INGOT)
			register(REVITALIZATION_SUBSTANCE)
			register(BINDING_ESSENCE)
			
			register(COMPOST)
			register(VOID_SALAD)
			
			register(TABLE_LINK)
			register(KNOWLEDGE_NOTE)
			register(ENDERMAN_HEAD)
			
			register(VOID_MINER)
			register(VOID_BUCKET)
			register(SCORCHING_PICKAXE)
			register(SCORCHING_SHOVEL)
			register(SCORCHING_AXE)
			register(SCORCHING_SWORD)
			register(FLINT_AND_INFERNIUM)
			
			register(ENDER_GOO_BUCKET)
			register(PURIFIED_ENDER_GOO_BUCKET)
			
			register(ENERGY_ORACLE)
			register(ENERGY_RECEPTACLE)
			
			register(INFUSED_ENDER_PEARL)
			register(SPATIAL_DASH_GEM)
			register(PORTAL_TOKEN)
			register(BLANK_TOKEN)
			
			register(TRINKET_POUCH)
			register(TOTEM_OF_UNDYING)
			register(AMULET_OF_RECOVERY)
			register(RING_OF_HUNGER)
			register(RING_OF_PRESERVATION)
			register(TALISMAN_OF_GRIEFING)
			register(SCALE_OF_FREEFALL)
		}
		
		// vanilla modifications
		
		Items.CHORUS_FRUIT_POPPED.creativeTab = null
		Items.ELYTRA.creativeTab = null
		
		with(e.registry){
			register(ItemChorusBerry().apply { override(Items.CHORUS_FRUIT) })
			register(ItemElytraOverride().apply { override(Items.ELYTRA) })
			register(ItemEyeOfEnderOverride().apply { override(Items.ENDER_EYE) })
			register(ItemTotemOfUndyingOverride().apply { override(Items.TOTEM_OF_UNDYING, newCreativeTab = null) })
			
			for(color in EnumDyeColor.values()){
				val box = BlockShulkerBox.getBlockByColor(color)
				register(ItemShulkerBoxOverride(box).apply { override(Item.getItemFromBlock(box)) })
			}
		}
		
		// dispenser behavior
		
		with(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY){
			putObject(ENDERMAN_HEAD, DispenseEndermanHead)
			putObject(Items.WATER_BUCKET, DispenseWaterExtinguishIgneousPlate(getObject(Items.WATER_BUCKET)))
		}
	}
	
	// Utilities
	
	private fun Item.setup(registryName: String, translationKey: String = "hee.$registryName", inCreativeTab: Boolean = true){
		this.registryName = Resource.Custom(registryName)
		this.translationKey = translationKey
		
		if (inCreativeTab){
			this.creativeTab = ModCreativeTabs.main.also { it.registerOrder(this) }
		}
	}
	
	private fun Item.override(vanillaItem: Item, newCreativeTab: CreativeTabs? = ModCreativeTabs.main){
		this.useVanillaName(vanillaItem)
		this.creativeTab = newCreativeTab
		
		if (newCreativeTab is OrderedCreativeTab){
			newCreativeTab.registerOrder(this)
		}
	}
}
