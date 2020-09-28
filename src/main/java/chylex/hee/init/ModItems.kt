package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.dispenser.DispenseEndermanHead
import chylex.hee.game.block.dispenser.DispenseExperienceBottle
import chylex.hee.game.block.dispenser.DispenseWaterExtinguishIgneousPlate
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.item.ItemAmuletOfRecovery
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemBlockHead
import chylex.hee.game.item.ItemBucketWithCauldron
import chylex.hee.game.item.ItemChorusBerry
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemElytraOverride
import chylex.hee.game.item.ItemEndPowder
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemExperienceBottleCustom
import chylex.hee.game.item.ItemEyeOfEnderOverride
import chylex.hee.game.item.ItemFlintAndInfernium
import chylex.hee.game.item.ItemIgneousRock
import chylex.hee.game.item.ItemInfusedEnderPearl
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemPurifiedEnderGooBucket
import chylex.hee.game.item.ItemPuzzleMedallion
import chylex.hee.game.item.ItemRevitalizationSubstance
import chylex.hee.game.item.ItemRingOfHunger
import chylex.hee.game.item.ItemRingOfPreservation
import chylex.hee.game.item.ItemScaleOfFreefall
import chylex.hee.game.item.ItemScorchingSword
import chylex.hee.game.item.ItemScorchingTool
import chylex.hee.game.item.ItemSpatialDashGem
import chylex.hee.game.item.ItemTableCore
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.item.ItemTalismanOfGriefing
import chylex.hee.game.item.ItemTotemOfUndyingCustom
import chylex.hee.game.item.ItemTotemOfUndyingOverride
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidMiner
import chylex.hee.game.item.ItemVoidSalad
import chylex.hee.game.item.Tool.Type.AXE
import chylex.hee.game.item.Tool.Type.PICKAXE
import chylex.hee.game.item.Tool.Type.SHOVEL
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.useVanillaName
import chylex.hee.system.migration.BlockDispenser
import chylex.hee.system.migration.ItemSpawnEgg
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.Items
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModItems{
	private val baseProps
		get() = Item.Properties().group(ModCreativeTabs.main)
	
	private val defaultProps = baseProps
	private val toolProps = baseProps.maxStackSize(1)
	
	// Items: Raw resources
	
	@JvmField val ETHEREUM          = Item(defaultProps) named "ethereum"
	@JvmField val ANCIENT_DUST      = Item(defaultProps) named "ancient_dust"
	@JvmField val END_POWDER        = ItemEndPowder(defaultProps) named "end_powder"
	@JvmField val STARDUST          = Item(defaultProps) named "stardust"
	@JvmField val ENDIUM_INGOT      = Item(defaultProps) named "endium_ingot"
	@JvmField val ENDIUM_NUGGET     = Item(defaultProps) named "endium_nugget"
	@JvmField val OBSIDIAN_FRAGMENT = Item(defaultProps) named "obsidian_fragment"
	@JvmField val IGNEOUS_ROCK      = ItemIgneousRock(defaultProps) named "igneous_rock"
	@JvmField val PUZZLE_MEDALLION  = ItemPuzzleMedallion(defaultProps) named "puzzle_medallion"
	@JvmField val INFERNIUM         = Item(defaultProps) named "infernium"
	@JvmField val INFERNIUM_INGOT   = Item(defaultProps) named "infernium_ingot"
	@JvmField val AURICION          = Item(defaultProps) named "auricion"
	@JvmField val DRAGON_SCALE      = Item(defaultProps) named "dragon_scale"
	@JvmField val INSTABILITY_ORB   = Item(defaultProps) named "instability_orb"
	@JvmField val ECTOPLASM         = Item(defaultProps) named "ectoplasm"
	@JvmField val ENCHANTED_CLAW    = Item(defaultProps) named "enchanted_claw"
	
	// Items: Manufactured resources
	
	@JvmField val ALTERATION_NEXUS         = Item(defaultProps) named "alteration_nexus"
	@JvmField val VOID_ESSENCE             = Item(defaultProps) named "void_essence"
	@JvmField val OBSIDIAN_ROD             = Item(defaultProps) named "obsidian_rod"
	@JvmField val PURITY_EXTRACT           = Item(defaultProps) named "purity_extract"
	@JvmField val STATIC_CORE              = Item(defaultProps) named "static_core"
	@JvmField val TICKING_CORE             = Item(defaultProps) named "ticking_core"
	@JvmField val DIRTY_INFERNIUM_INGOT    = Item(defaultProps) named "dirty_infernium_ingot"
	@JvmField val AMELIOR                  = Item(defaultProps) named "amelior"
	@JvmField val REVITALIZATION_SUBSTANCE = ItemRevitalizationSubstance(baseProps.maxStackSize(16)) named "revitalization_substance"
	@JvmField val BINDING_ESSENCE          = ItemBindingEssence(baseProps.maxStackSize(16)) named "binding_essence"
	
	// Items: Nature & food
	
	@JvmField val COMPOST    = ItemCompost(defaultProps) named "compost"
	@JvmField val VOID_SALAD = ItemVoidSalad(baseProps.maxStackSize(1).food(ItemVoidSalad.FOOD)) named "void_salad"
	
	// Items: Table cores
	
	@JvmField val ACCUMULATION_TABLE_CORE = ItemTableCore(arrayOf(ModBlocks.ACCUMULATION_TABLE_TIER_1, ModBlocks.ACCUMULATION_TABLE_TIER_2, ModBlocks.ACCUMULATION_TABLE_TIER_3), defaultProps) named "accumulation_table_core"
	@JvmField val EXPERIENCE_TABLE_CORE   = ItemTableCore(arrayOf(ModBlocks.EXPERIENCE_TABLE_TIER_1, ModBlocks.EXPERIENCE_TABLE_TIER_2, ModBlocks.EXPERIENCE_TABLE_TIER_3), defaultProps) named "experience_table_core"
	@JvmField val INFUSION_TABLE_CORE     = ItemTableCore(arrayOf(ModBlocks.INFUSION_TABLE_TIER_1, ModBlocks.INFUSION_TABLE_TIER_2, ModBlocks.INFUSION_TABLE_TIER_3), defaultProps) named "infusion_table_core"
	
	// Items: Utilities
	
	@JvmField val TABLE_LINK        = ItemTableLink(defaultProps) named "table_link"
	@JvmField val KNOWLEDGE_NOTE    = Item(defaultProps) named "knowledge_note" // TODO
	@JvmField val ENDERMAN_HEAD     = ItemBlockHead(ModBlocks.ENDERMAN_HEAD, ModBlocks.ENDERMAN_WALL_HEAD, defaultProps) named "enderman_head"
	@JvmField val EXPERIENCE_BOTTLE = ItemExperienceBottleCustom(defaultProps) named "experience_bottle"
	
	// Items: Tools
	
	@JvmField val VOID_MINER          = ItemVoidMiner(baseProps.maxStackSize(1).setNoRepair()) named "void_miner"
	@JvmField val VOID_BUCKET         = ItemVoidBucket(baseProps.maxStackSize(1).setNoRepair()) named "void_bucket"
	@JvmField val SCORCHING_PICKAXE   = ItemScorchingTool(baseProps.setNoRepair(), PICKAXE, attackSpeed = -2.8F) named "scorching_pickaxe"
	@JvmField val SCORCHING_SHOVEL    = ItemScorchingTool(baseProps.setNoRepair(), SHOVEL, attackSpeed = -3F) named "scorching_shovel"
	@JvmField val SCORCHING_AXE       = ItemScorchingTool(baseProps.setNoRepair(), AXE, attackSpeed = -3F) named "scorching_axe"
	@JvmField val SCORCHING_SWORD     = ItemScorchingSword(baseProps.setNoRepair()) named "scorching_sword"
	@JvmField val FLINT_AND_INFERNIUM = ItemFlintAndInfernium(baseProps.maxDamage(25)) named "flint_and_infernium"
	
	// Items: Fluids
	
	@JvmField val ENDER_GOO_BUCKET          = ItemBucketWithCauldron(FluidEnderGoo.still, ModBlocks.CAULDRON_ENDER_GOO, baseProps.maxStackSize(1).containerItem(Items.BUCKET)) named "ender_goo_bucket"
	@JvmField val PURIFIED_ENDER_GOO_BUCKET = ItemPurifiedEnderGooBucket(FluidEnderGooPurified.still, ModBlocks.CAULDRON_PURIFIED_ENDER_GOO, baseProps.maxStackSize(1).containerItem(Items.BUCKET)) named "purified_ender_goo_bucket"
	
	// Items: Energy
	
	@JvmField val ENERGY_ORACLE     = ItemEnergyOracle(toolProps) named "energy_oracle"
	@JvmField val ENERGY_RECEPTACLE = ItemEnergyReceptacle(toolProps) named "energy_receptacle"
	
	// Items: Gems & teleportation
	
	@JvmField val INFUSED_ENDER_PEARL = ItemInfusedEnderPearl(Item.Properties().maxStackSize(16)) named "infused_ender_pearl"
	@JvmField val SPATIAL_DASH_GEM    = ItemSpatialDashGem(toolProps) named "spatial_dash_gem"
	@JvmField val LINKING_GEM         = ItemSpatialDashGem(toolProps) named "linking_gem" // TODO
	@JvmField val PORTAL_TOKEN        = ItemPortalToken(baseProps.maxStackSize(ItemPortalToken.MAX_STACK_SIZE)) named "portal_token"
	@JvmField val BLANK_TOKEN         = Item(baseProps.maxStackSize(ItemPortalToken.MAX_STACK_SIZE)) named "blank_token"
	
	// Items: Trinkets
	
	@JvmField val TRINKET_POUCH        = ItemTrinketPouch(toolProps) named "trinket_pouch"
	@JvmField val TOTEM_OF_UNDYING     = ItemTotemOfUndyingCustom(baseProps.maxDamage(4)) named "totem_of_undying"
	@JvmField val AMULET_OF_RECOVERY   = ItemAmuletOfRecovery(toolProps) named "amulet_of_recovery"
	@JvmField val RING_OF_HUNGER       = ItemRingOfHunger(baseProps.maxDamage(120)) named "ring_of_hunger"
	@JvmField val RING_OF_PRESERVATION = ItemRingOfPreservation(baseProps.maxDamage(1)) named "ring_of_preservation"
	@JvmField val TALISMAN_OF_GRIEFING = ItemTalismanOfGriefing(baseProps.maxDamage(25)) named "talisman_of_griefing"
	@JvmField val SCALE_OF_FREEFALL    = ItemScaleOfFreefall(baseProps.maxDamage(8)) named "scale_of_freefall"
	
	// Items: Spawn Eggs
	
	@JvmField val SPAWN_ENDER_EYE             = ItemSpawnEgg(ModEntities.ENDER_EYE, RGB(22u).i, RGB(219, 58, 115).i, baseProps) named "ender_eye_spawn_egg"
	@JvmField val SPAWN_ANGRY_ENDERMAN        = ItemSpawnEgg(ModEntities.ANGRY_ENDERMAN, RGB(21u).i, RGB(111, 75, 36).i, baseProps) named "angry_enderman_spawn_egg"
	@JvmField val SPAWN_BLOBBY                = ItemSpawnEgg(ModEntities.BLOBBY, RGB(103, 140, 94).i, RGB(255u).i, baseProps) named "blobby_spawn_egg"
	@JvmField val SPAWN_ENDERMITE_INSTABILITY = ItemSpawnEgg(ModEntities.ENDERMITE_INSTABILITY, RGB(21u).i, RGB(94, 122, 108).i, baseProps) named "endermite_instability_spawn_egg"
	@JvmField val SPAWN_SPIDERLING            = ItemSpawnEgg(ModEntities.SPIDERLING, RGB(32, 30, 16).i, RGB(182, 25, 0).i, baseProps) named "spiderling_spawn_egg"
	@JvmField val SPAWN_UNDREAD               = ItemSpawnEgg(ModEntities.UNDREAD, TerritoryType.FORGOTTEN_TOMBS.desc.colors.tokenTop.i, TerritoryType.FORGOTTEN_TOMBS.desc.colors.tokenBottom.i, baseProps) named "undread_spawn_egg"
	@JvmField val SPAWN_VAMPIRE_BAT           = ItemSpawnEgg(ModEntities.VAMPIRE_BAT, RGB(76, 62, 48).i, RGB(66, 16, 15).i, baseProps) named "vampire_bat_spawn_egg"
	
	// Registry
	
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
			register(INSTABILITY_ORB)
			register(ECTOPLASM)
			register(ENCHANTED_CLAW)
			
			register(ALTERATION_NEXUS)
			register(VOID_ESSENCE)
			register(OBSIDIAN_ROD)
			register(PURITY_EXTRACT)
			register(STATIC_CORE)
			register(TICKING_CORE)
			register(DIRTY_INFERNIUM_INGOT)
			register(AMELIOR)
			register(REVITALIZATION_SUBSTANCE)
			register(BINDING_ESSENCE)
			
			register(COMPOST)
			register(VOID_SALAD)
			
			register(ACCUMULATION_TABLE_CORE)
			register(EXPERIENCE_TABLE_CORE)
			register(INFUSION_TABLE_CORE)
			
			register(TABLE_LINK)
			register(KNOWLEDGE_NOTE)
			register(ENDERMAN_HEAD)
			register(EXPERIENCE_BOTTLE)
			
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
			register(LINKING_GEM)
			register(PORTAL_TOKEN)
			register(BLANK_TOKEN)
			
			register(TRINKET_POUCH)
			register(TOTEM_OF_UNDYING)
			register(AMULET_OF_RECOVERY)
			register(RING_OF_HUNGER)
			register(RING_OF_PRESERVATION)
			register(TALISMAN_OF_GRIEFING)
			register(SCALE_OF_FREEFALL)
			
			register(SPAWN_ENDER_EYE)
			register(SPAWN_ANGRY_ENDERMAN)
			register(SPAWN_BLOBBY)
			register(SPAWN_ENDERMITE_INSTABILITY)
			register(SPAWN_SPIDERLING)
			register(SPAWN_UNDREAD)
			register(SPAWN_VAMPIRE_BAT)
		}
		
		// vanilla modifications
		
		Items.POPPED_CHORUS_FRUIT.group = null
		Items.ELYTRA.group = null
		
		with(e.registry){
			register(ItemChorusBerry(baseProps.food(ItemChorusBerry.FOOD)).apply { override(Items.CHORUS_FRUIT) })
			register(ItemElytraOverride(baseProps.maxDamage(432)).apply { override(Items.ELYTRA) })
			register(ItemEyeOfEnderOverride(defaultProps).apply { override(Items.ENDER_EYE) })
			register(ItemTotemOfUndyingOverride(toolProps).apply { override(Items.TOTEM_OF_UNDYING, newCreativeTab = null) })
		}
		
		// dispenser behavior
		
		BlockDispenser.registerDispenseBehavior(ENDERMAN_HEAD, DispenseEndermanHead)
		BlockDispenser.registerDispenseBehavior(EXPERIENCE_BOTTLE, DispenseExperienceBottle)
		BlockDispenser.registerDispenseBehavior(Items.WATER_BUCKET, DispenseWaterExtinguishIgneousPlate(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY[Items.WATER_BUCKET]))
	}
	
	// Utilities
	
	private infix fun <T : Item> T.named(registryName: String): T = apply {
		this.registryName = Resource.Custom(registryName)
		(this.group as? OrderedCreativeTab)?.registerOrder(this)
	}
	
	private fun Item.override(vanillaItem: Item, newCreativeTab: ItemGroup? = ModCreativeTabs.main){
		this.useVanillaName(vanillaItem)
		this.group = newCreativeTab
		
		if (newCreativeTab is OrderedCreativeTab){
			newCreativeTab.registerOrder(this)
		}
	}
}
