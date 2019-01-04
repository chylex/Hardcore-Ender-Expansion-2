package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.item.ItemAmuletOfRecovery
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemChorusBerry
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemElytraOverride
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemIgneousRock
import chylex.hee.game.item.ItemRingOfHunger
import chylex.hee.game.item.ItemRingOfPreservation
import chylex.hee.game.item.ItemScaleOfFreefall
import chylex.hee.game.item.ItemScorchingTool
import chylex.hee.game.item.ItemSpatialDashGem
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.item.ItemTalismanOfGriefing
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidMiner
import chylex.hee.game.item.ItemVoidSalad
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.Resource
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemBucket
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
object ModItems{
	
	// Items: Raw resources
	
	@JvmField val ETHEREUM          = Item().apply { setup("ethereum") }
	@JvmField val ANCIENT_DUST      = Item().apply { setup("ancient_dust") }
	@JvmField val END_POWDER        = Item().apply { setup("end_powder") }
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
	
	@JvmField val ALTERATION_NEXUS      = Item().apply { setup("alteration_nexus") }
	@JvmField val VOID_ESSENCE          = Item().apply { setup("void_essence") }
	@JvmField val OBSIDIAN_ROD          = Item().apply { setup("obsidian_rod") }
	@JvmField val STATIC_CORE           = Item().apply { setup("static_core") }
	@JvmField val TABLE_LINK            = ItemTableLink().apply { setup("table_link") }
	@JvmField val DIRTY_INFERNIUM_INGOT = Item().apply { setup("dirty_infernium_ingot") }
	@JvmField val BINDING_ESSENCE       = ItemBindingEssence().apply { setup("binding_essence") }
	
	// Items: Nature & food
	
	@JvmField val COMPOST    = ItemCompost().apply { setup("compost") }
	@JvmField val VOID_SALAD = ItemVoidSalad().apply { setup("void_salad") }
	
	// Items: Tools
	
	@JvmField val VOID_MINER        = ItemVoidMiner().apply { setup("void_miner") }
	@JvmField val VOID_BUCKET       = ItemVoidBucket().apply { setup("void_bucket") }
	@JvmField val SCORCHING_PICKAXE = ItemScorchingTool(PICKAXE).apply { setup("scorching_pickaxe") }
	@JvmField val SCORCHING_SHOVEL  = ItemScorchingTool(SHOVEL).apply { setup("scorching_shovel") }
	@JvmField val SCORCHING_AXE     = ItemScorchingTool(AXE).apply { setup("scorching_axe") } // TODO remove?
	
	// Items: Buckets
	
	@JvmField val ENDER_GOO_BUCKET = ItemBucket(ModBlocks.ENDER_GOO).apply { setup("ender_goo_bucket"); containerItem = Items.BUCKET }
	
	// Items: Energy
	
	@JvmField val ENERGY_ORACLE     = ItemEnergyOracle().apply { setup("energy_oracle") }
	@JvmField val ENERGY_RECEPTACLE = ItemEnergyReceptacle().apply { setup("energy_receptacle") }
	
	// Items: Gems
	
	@JvmField val SPATIAL_DASH_GEM = ItemSpatialDashGem().apply { setup("spatial_dash_gem") }
	
	// Items: Trinkets
	
	@JvmField val TRINKET_POUCH        = ItemTrinketPouch().apply { setup("trinket_pouch") }
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
			register(STATIC_CORE)
			register(TABLE_LINK)
			register(DIRTY_INFERNIUM_INGOT)
			register(BINDING_ESSENCE)
			
			register(COMPOST)
			register(VOID_SALAD)
			
			register(VOID_MINER)
			register(VOID_BUCKET)
			register(SCORCHING_PICKAXE)
			register(SCORCHING_SHOVEL)
			register(SCORCHING_AXE)
			
			register(ENDER_GOO_BUCKET)
			
			register(ENERGY_ORACLE)
			register(ENERGY_RECEPTACLE)
			
			register(SPATIAL_DASH_GEM)
			
			register(TRINKET_POUCH)
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
		}
	}
	
	// Utilities
	
	private fun Item.setup(registryName: String, translationKey: String = registryName, inCreativeTab: Boolean = true){
		this.registryName = Resource.Custom(registryName)
		this.translationKey = "hee.$translationKey"
		
		if (inCreativeTab){
			this.creativeTab = ModCreativeTabs.main.also { it.registerOrder(this) }
		}
	}
	
	private fun Item.override(vanillaItem: Item, newCreativeTab: CreativeTabs? = ModCreativeTabs.main){
		this.registryName = vanillaItem.registryName
		this.creativeTab = newCreativeTab
		
		if (newCreativeTab is OrderedCreativeTab){
			newCreativeTab.registerOrder(this)
		}
	}
}
