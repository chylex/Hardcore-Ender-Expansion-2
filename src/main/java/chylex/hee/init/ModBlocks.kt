package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.game.block.BlockChorusFlowerOverride
import chylex.hee.game.block.BlockChorusPlantOverride
import chylex.hee.game.block.BlockCorruptedEnergy
import chylex.hee.game.block.BlockDarkChest
import chylex.hee.game.block.BlockDeathFlowerDecaying
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockDustyStoneBricks
import chylex.hee.game.block.BlockDustyStonePlain
import chylex.hee.game.block.BlockEndPlant
import chylex.hee.game.block.BlockEndPortalAcceptor
import chylex.hee.game.block.BlockEndPortalInner
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.block.BlockEndPowderOre
import chylex.hee.game.block.BlockEnderGoo
import chylex.hee.game.block.BlockEndermanHead
import chylex.hee.game.block.BlockEndersol
import chylex.hee.game.block.BlockEndium
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.BlockFallingObsidian
import chylex.hee.game.block.BlockGloomrock
import chylex.hee.game.block.BlockGloomtorch
import chylex.hee.game.block.BlockHumus
import chylex.hee.game.block.BlockIgneousRockOre
import chylex.hee.game.block.BlockInfusedGlass
import chylex.hee.game.block.BlockInfusedTNT
import chylex.hee.game.block.BlockLootChest
import chylex.hee.game.block.BlockPillarCustom
import chylex.hee.game.block.BlockScaffolding
import chylex.hee.game.block.BlockSimple
import chylex.hee.game.block.BlockSimpleShaped
import chylex.hee.game.block.BlockSlabCustom
import chylex.hee.game.block.BlockStairsCustom
import chylex.hee.game.block.BlockStardustOre
import chylex.hee.game.block.BlockTableBase
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTableTile
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.BlockVoidPortalStorage
import chylex.hee.game.block.BlockWallCustom
import chylex.hee.game.block.BlockWhitebarkLog
import chylex.hee.game.block.entity.TileEntityAccumulationTable
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.material.Materials
import chylex.hee.game.item.ItemAncientCobweb
import chylex.hee.game.item.ItemBlockSlabFuel
import chylex.hee.game.item.ItemBlockWithMetadata
import chylex.hee.game.item.ItemDragonEgg
import chylex.hee.game.item.ItemInfusedTNT
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.Resource
import chylex.hee.system.util.clone
import chylex.hee.system.util.useVanillaName
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSlab
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

@EventBusSubscriber(modid = HEE.ID)
object ModBlocks{
	init{
		FluidRegistry.registerFluid(FluidEnderGoo)
	}
	
	// Blocks: Building (Uncategorized)
	
	private val buildEtherealLantern = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.9F
		
		lightLevel = 15
		
		soundType = SoundType.GLASS
		mapColor = MapColor.PURPLE
	}
	
	private val buildInfusedGlass = BlockSimple.Builder(Material.GLASS).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.5F
		explosionResistance = 0.6F
		
		soundType = SoundType.GLASS
		mapColor = MapColor.ORANGE_STAINED_HARDENED_CLAY
	}
	
	private val buildVantablock = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 10.0F
		explosionResistance = 1.0F
		
		soundType = SoundType.CLOTH
		mapColor = MapColor.BLACK
	}
	
	private val buildEndiumBlock = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 6.2F
		explosionResistance = 20.0F
		
		soundType = SoundType.METAL
		mapColor = MapColor.BLUE
	}
	
	private val buildEnderSol = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.9F
		
		soundType = SoundType.GROUND.clone(pitch = 0.85F)
		mapColor = MapColor.WOOD
	}
	
	private val buildHumus = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.3F
		
		soundType = SoundType.GROUND
		mapColor = MapColor.BLACK
	}
	
	@JvmField val ETHEREAL_LANTERN = BlockSimple(buildEtherealLantern).apply { setup("ethereal_lantern") }
	@JvmField val STONE_BRICK_WALL = BlockWallCustom(Blocks.STONEBRICK).apply { setup("stone_brick_wall") }
	@JvmField val INFUSED_GLASS    = BlockInfusedGlass(buildInfusedGlass).apply { setup("infused_glass") }
	@JvmField val VANTABLOCK       = BlockSimple(buildVantablock).apply { setup("vantablock") }
	@JvmField val ENDIUM_BLOCK     = BlockEndium(buildEndiumBlock).apply { setup("endium_block") }
	@JvmField val ENDERSOL         = BlockEndersol(buildEnderSol).apply { setup("endersol") }
	@JvmField val HUMUS            = BlockHumus(buildHumus).apply { setup("humus") }
	
	// Blocks: Building (Gloomrock)
	
	private val buildGloomrock = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 7.0F
		
		soundType = SoundType.STONE
		mapColor = MapColor.BLACK
	}
	
	private val buildGloomrockBricks = buildGloomrock.clone {
		harvestHardness = 2.8F
		explosionResistance = 10.0F
	}
	
	private val buildGloomrockSmooth = buildGloomrock.clone {
		harvestHardness = 2.0F
		explosionResistance = 8.0F
	}
	
	private val buildGloomtorch = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		explosionResistance = 0.5F
		
		lightLevel = 13
		
		soundType = SoundType.STONE
		mapColor = MapColor.BLACK
	}
	
	@JvmField val GLOOMROCK                    = BlockGloomrock(buildGloomrock).apply { setup("gloomrock") }
	@JvmField val GLOOMROCK_BRICKS             = BlockGloomrock(buildGloomrockBricks).apply { setup("gloomrock_bricks") }
	@JvmField val GLOOMROCK_BRICK_STAIRS       = BlockStairsCustom(GLOOMROCK_BRICKS).apply { setup("gloomrock_brick_stairs") }
	@JvmField val GLOOMROCK_BRICK_SLAB         = BlockSlabCustom.Half(buildGloomrockBricks).apply { setup("gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_BRICK_DOUBLE_SLAB  = BlockSlabCustom.Full(buildGloomrockBricks, GLOOMROCK_BRICK_SLAB).apply { setup("gloomrock_brick_slab_double", "hee.gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_SMOOTH             = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth") }
	@JvmField val GLOOMROCK_SMOOTH_STAIRS      = BlockStairsCustom(GLOOMROCK_SMOOTH).apply { setup("gloomrock_smooth_stairs") }
	@JvmField val GLOOMROCK_SMOOTH_SLAB        = BlockSlabCustom.Half(buildGloomrockSmooth).apply { setup("gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_DOUBLE_SLAB = BlockSlabCustom.Full(buildGloomrockSmooth, GLOOMROCK_SMOOTH_SLAB).apply { setup("gloomrock_smooth_slab_double", "hee.gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_RED         = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_red") }
	@JvmField val GLOOMROCK_SMOOTH_ORANGE      = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_orange") }
	@JvmField val GLOOMROCK_SMOOTH_YELLOW      = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_yellow") }
	@JvmField val GLOOMROCK_SMOOTH_GREEN       = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_green") }
	@JvmField val GLOOMROCK_SMOOTH_CYAN        = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_cyan") }
	@JvmField val GLOOMROCK_SMOOTH_BLUE        = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_blue") }
	@JvmField val GLOOMROCK_SMOOTH_PURPLE      = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_purple") }
	@JvmField val GLOOMROCK_SMOOTH_MAGENTA     = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_magenta") }
	@JvmField val GLOOMROCK_SMOOTH_WHITE       = BlockGloomrock(buildGloomrockSmooth).apply { setup("gloomrock_smooth_white") }
	@JvmField val GLOOMTORCH                   = BlockGloomtorch(buildGloomtorch).apply { setup("gloomtorch") }
	
	// Blocks: Building (Dusty Stone)
	
	private val buildDustyStone = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		harvestTool = Pair(WOOD, PICKAXE) // + shovel
		harvestHardness = 1.1F
		explosionResistance = 0.5F
		
		soundType = SoundType.STONE
		mapColor = MapColor.SAND
	}
	
	private val buildDustyStoneCracked = buildDustyStone.clone {
		harvestHardness = 1.0F
		explosionResistance = 0.2F
	}
	
	private val buildDustyStoneDamaged = buildDustyStone.clone {
		harvestHardness = 0.9F
		explosionResistance = 0.1F
	}
	
	private val buildDustyStoneBricks = buildDustyStone.clone {
		harvestHardness = 1.9F
		explosionResistance = 3.0F
	}
	
	@JvmField val DUSTY_STONE                = BlockDustyStonePlain(buildDustyStone).apply { setup("dusty_stone") }
	@JvmField val DUSTY_STONE_CRACKED        = BlockDustyStonePlain(buildDustyStoneCracked).apply { setup("dusty_stone_cracked") }
	@JvmField val DUSTY_STONE_DAMAGED        = BlockDustyStonePlain(buildDustyStoneDamaged).apply { setup("dusty_stone_damaged") }
	@JvmField val DUSTY_STONE_BRICKS         = BlockDustyStoneBricks(buildDustyStoneBricks).apply { setup("dusty_stone_bricks") } // UPDATE: update recipe json to include tag to allow all dusty stone variations
	@JvmField val DUSTY_STONE_CRACKED_BRICKS = BlockDustyStoneBricks(buildDustyStoneBricks).apply { setup("dusty_stone_cracked_bricks") }
	
	// Blocks: Building (Obsidian)
	
	private val buildObsidian = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 50F
		explosionResistance = 2000F
		
		soundType = SoundType.STONE
		mapColor = MapColor.BLACK
	}
	
	private val buildObsidianVariation = buildObsidian.clone {
		harvestHardness = 20F
		explosionResistance = 500F
	}
	
	private val buildObsidianVariationLit = buildObsidianVariation.clone {
		lightLevel = 15
	}
	
	@JvmField val OBSIDIAN_STAIRS       = BlockStairsCustom(Blocks.OBSIDIAN).apply { setup("obsidian_stairs") }
	@JvmField val OBSIDIAN_FALLING      = BlockFallingObsidian(buildObsidian).apply { setup("obsidian_falling") }
	@JvmField val OBSIDIAN_SMOOTH       = BlockSimple(buildObsidianVariation).apply { setup("obsidian_smooth") }
	@JvmField val OBSIDIAN_CHISELED     = BlockSimple(buildObsidianVariation).apply { setup("obsidian_chiseled") }
	@JvmField val OBSIDIAN_PILLAR       = BlockPillarCustom(buildObsidianVariation).apply { setup("obsidian_pillar") }
	@JvmField val OBSIDIAN_SMOOTH_LIT   = BlockSimple(buildObsidianVariationLit).apply { setup("obsidian_smooth_lit") }
	@JvmField val OBSIDIAN_CHISELED_LIT = BlockSimple(buildObsidianVariationLit).apply { setup("obsidian_chiseled_lit") }
	@JvmField val OBSIDIAN_PILLAR_LIT   = BlockPillarCustom(buildObsidianVariationLit).apply { setup("obsidian_pillar_lit") }
	
	// Blocks: Building (End Stone)
	
	private val buildEndStone = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 3.0F
		explosionResistance = 15.0F
		
		soundType = SoundType.STONE
		mapColor = MapColor.SAND
	}
	
	@JvmField val END_STONE_INFESTED  = BlockSimple(buildEndStone).apply { setup("end_stone_infested") }
	@JvmField val END_STONE_BURNED    = BlockSimple(buildEndStone).apply { setup("end_stone_burned") }
	@JvmField val END_STONE_ENCHANTED = BlockSimple(buildEndStone).apply { setup("end_stone_enchanted") }
	
	// Blocks: Building (Dark Loam)
	
	private val buildDarkLoam = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.6F
		
		soundType = SoundType.GROUND
		mapColor = MapColor.BLACK
	}
	
	@JvmField val DARK_LOAM             = BlockSimple(buildDarkLoam).apply { setup("dark_loam") }
	@JvmField val DARK_LOAM_SLAB        = BlockSlabCustom.Half(buildDarkLoam).apply { setup("dark_loam_slab") }
	@JvmField val DARK_LOAM_DOUBLE_SLAB = BlockSlabCustom.Full(buildDarkLoam, DARK_LOAM_SLAB).apply { setup("dark_loam_slab_double", "hee.dark_loam_slab") }
	
	// Blocks: Building (Wood)
	
	private val buildWhitebark = BlockSimple.Builder(Material.WOOD).apply {
		harvestTool = Pair(WOOD, AXE)
		harvestHardness = 2.0F
		
		soundType = SoundType.WOOD
		mapColor = MapColor.SNOW
	}
	
	private val buildWhitebarkPlanks = buildWhitebark.clone {
		explosionResistance = 5.0F
	}
	
	@JvmField val WHITEBARK_LOG          = BlockWhitebarkLog().apply { setup("whitebark_log") }
	@JvmField val WHITEBARK              = BlockSimple(buildWhitebark).apply { setup("whitebark") }
	@JvmField val WHITEBARK_PLANKS       = BlockSimple(buildWhitebarkPlanks).apply { setup("whitebark_planks") }
	@JvmField val WHITEBARK_STAIRS       = BlockStairsCustom(WHITEBARK_PLANKS).apply { setup("whitebark_stairs") }
	@JvmField val WHITEBARK_SLAB         = BlockSlabCustom.Half(buildWhitebarkPlanks).apply { setup("whitebark_slab") }
	@JvmField val WHITEBARK_DOUBLE_SLAB  = BlockSlabCustom.Full(buildWhitebarkPlanks, WHITEBARK_SLAB).apply { setup("whitebark_slab_double", "hee.whitebark_slab") }
	
	// Blocks: Interactive (Uncategorized)
	
	private val buildLootChest = BlockSimple.Builder(Materials.SOLID_NO_TOOL).apply {
		makeIndestructible()
		
		lightLevel = 13
		
		soundType = SoundType.METAL
		mapColor = MapColor.BLACK
	}
	
	@JvmField val ENDER_GOO   = BlockEnderGoo().apply { setup("ender_goo") }
	@JvmField val INFUSED_TNT = BlockInfusedTNT().apply { setup("infused_tnt", translationKey = "tnt", inCreativeTab = false) }
	@JvmField val DARK_CHEST  = BlockDarkChest(buildGloomrock).apply { setup("dark_chest") } // UPDATE: update recipe json to include tag to allow either slab variation
	@JvmField val LOOT_CHEST  = BlockLootChest(buildLootChest).apply { setup("loot_chest") }
	
	// Blocks: Ores
	
	private val buildEndOre = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		soundType = SoundType.STONE
		mapColor = MapColor.SAND
	}
	
	private val buildEndPowderOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.0F
		explosionResistance = 9.0F
	}
	
	private val buildEndiumOre = buildEndOre.clone {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 5.0F
		explosionResistance = 16.5F
	}
	
	private val buildStardustOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.8F
		explosionResistance = 14.0F
	}
	
	private val buildIgneousRockOre = buildEndOre.clone {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 6.0F
	}
	
	@JvmField val END_POWDER_ORE   = BlockEndPowderOre(buildEndPowderOre).apply { setup("end_powder_ore") }
	@JvmField val ENDIUM_ORE       = BlockEndium(buildEndiumOre).apply { setup("endium_ore") }
	@JvmField val STARDUST_ORE     = BlockStardustOre(buildStardustOre).apply { setup("stardust_ore") }
	@JvmField val IGNEOUS_ROCK_ORE = BlockIgneousRockOre(buildIgneousRockOre).apply { setup("igneous_rock_ore") }
	
	// Blocks: Decorative (Plants)
	
	@JvmField val DEATH_FLOWER_DECAYING = BlockDeathFlowerDecaying().apply { setup("death_flower") }
	@JvmField val DEATH_FLOWER_HEALED   = BlockEndPlant().apply { setup("death_flower_healed") }
	@JvmField val DEATH_FLOWER_WITHERED = BlockEndPlant().apply { setup("death_flower_withered") }
	
	// Blocks: Decorative (Uncategorized)
	
	@JvmField val ANCIENT_COBWEB = BlockAncientCobweb().apply { setup("ancient_cobweb") }
	@JvmField val DRY_VINES      = BlockDryVines().apply { setup("dry_vines") }
	@JvmField val ENDERMAN_HEAD  = BlockEndermanHead().apply { setup("enderman_head_block", inCreativeTab = false) }
	
	// Blocks: Portals
	
	private val buildPortalInner = BlockSimple.Builder(Material.PORTAL).apply {
		makeIndestructible()
		lightLevel = 15
		mapColor = MapColor.BLACK
	}
	
	private val buildPortalFrame = BlockSimple.Builder(Material.PORTAL).apply {
		makeIndestructible()
		mapColor = MapColor.SAND
	}
	
	private val portalFrameAABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0)
	
	@JvmField val END_PORTAL_INNER    = BlockEndPortalInner(buildPortalInner).apply { setup("end_portal_inner", inCreativeTab = false) }
	@JvmField val END_PORTAL_FRAME    = BlockSimpleShaped(buildPortalFrame, portalFrameAABB).apply { setup("end_portal_frame") }
	@JvmField val END_PORTAL_ACCEPTOR = BlockEndPortalAcceptor(buildPortalFrame, portalFrameAABB).apply { setup("end_portal_acceptor") }
	
	@JvmField val VOID_PORTAL_INNER   = BlockVoidPortalInner(buildPortalInner).apply { setup("void_portal_inner", inCreativeTab = false) }
	@JvmField val VOID_PORTAL_FRAME   = BlockSimpleShaped(buildPortalFrame, portalFrameAABB).apply { setup("void_portal_frame") }
	@JvmField val VOID_PORTAL_STORAGE = BlockVoidPortalStorage(buildPortalFrame, portalFrameAABB).apply { setup("void_portal_storage") }
	
	// Blocks: Energy
	
	private val buildEnergyCluster = BlockSimple.Builder(Materials.ENERGY_CLUSTER).apply {
		lightLevel = 13
		lightOpacity = 0
		
		soundType = SoundType.GLASS.clone(volume = 1.25F, pitch = 1.35F)
		mapColor = MapColor.SNOW
	}
	
	private val buildCorruptedEnergy = BlockSimple.Builder(Materials.CORRUPTED_ENERGY).apply {
		mapColor = MapColor.PURPLE
	}
	
	@JvmField val ENERGY_CLUSTER   = BlockEnergyCluster(buildEnergyCluster).apply { setup("energy_cluster") }
	@JvmField val CORRUPTED_ENERGY = BlockCorruptedEnergy(buildCorruptedEnergy).apply { setup("corrupted_energy") }
	
	// Blocks: Tables
	
	private val buildTablePedestal = buildGloomrock.clone {
		harvestHardness     = buildGloomrock.harvestHardness * 0.75F
		explosionResistance = buildGloomrock.explosionResistance * 0.5F
	}
	
	private val buildTable = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 20.0F
		explosionResistance = 25.0F
		
		soundType = SoundType.METAL
		mapColor = MapColor.GRAY
	}
	
	@JvmField val TABLE_PEDESTAL     = BlockTablePedestal(buildTablePedestal).apply { setup("table_pedestal") } // UPDATE: update recipe json to include tag to allow all gloomrock variations
	@JvmField val TABLE_BASE         = BlockTableBase(buildTable).apply { setup("table_base") }
	@JvmField val ACCUMULATION_TABLE = BlockTableTile(buildTable, ::TileEntityAccumulationTable, minAllowedTier = 1).apply { setup("accumulation_table") }
	
	// Blocks: Utilities
	
	private val buildScaffolding = BlockSimple.Builder(Materials.SCAFFOLDING).apply {
		makeIndestructible()
		
		lightOpacity = 0
		mapColor = MapColor.AIR
	}
	
	@JvmField val SCAFFOLDING = BlockScaffolding(buildScaffolding).apply { setup("scaffolding") }
	
	// Registry
	
	private val basicItemBlock = ::ItemBlock
	private val metaItemBlock = ::ItemBlockWithMetadata
	
	private fun slabItemBlock(half: BlockSlabCustom.Half, full: BlockSlabCustom.Full): ItemBlock{
		return ItemSlab(half, half, full).apply { hasSubtypes = false }
	}
	
	private fun woodenSlabItemBlock(half: BlockSlabCustom.Half, full: BlockSlabCustom.Full): ItemBlock{
		return ItemBlockSlabFuel(half, full, burnTicks = 150).apply { hasSubtypes = false }
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Block>){
		with(e.registry){
			register(ETHEREAL_LANTERN with basicItemBlock)
			register(STONE_BRICK_WALL with basicItemBlock)
			register(INFUSED_GLASS with basicItemBlock)
			register(VANTABLOCK with basicItemBlock)
			register(ENDIUM_BLOCK with basicItemBlock)
			register(ENDERSOL with basicItemBlock)
			register(HUMUS with basicItemBlock)
			
			register(GLOOMROCK with basicItemBlock)
			register(GLOOMROCK_BRICKS with basicItemBlock)
			register(GLOOMROCK_BRICK_STAIRS with basicItemBlock)
			register(GLOOMROCK_BRICK_SLAB with slabItemBlock(GLOOMROCK_BRICK_SLAB, GLOOMROCK_BRICK_DOUBLE_SLAB))
			register(GLOOMROCK_BRICK_DOUBLE_SLAB)
			register(GLOOMROCK_SMOOTH with basicItemBlock)
			register(GLOOMROCK_SMOOTH_STAIRS with basicItemBlock)
			register(GLOOMROCK_SMOOTH_SLAB with slabItemBlock(GLOOMROCK_SMOOTH_SLAB, GLOOMROCK_SMOOTH_DOUBLE_SLAB))
			register(GLOOMROCK_SMOOTH_DOUBLE_SLAB)
			register(GLOOMROCK_SMOOTH_RED with basicItemBlock)
			register(GLOOMROCK_SMOOTH_ORANGE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_YELLOW with basicItemBlock)
			register(GLOOMROCK_SMOOTH_GREEN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_CYAN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_BLUE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_PURPLE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_MAGENTA with basicItemBlock)
			register(GLOOMROCK_SMOOTH_WHITE with basicItemBlock)
			register(GLOOMTORCH with basicItemBlock)
			
			register(DUSTY_STONE with basicItemBlock)
			register(DUSTY_STONE_CRACKED with basicItemBlock)
			register(DUSTY_STONE_DAMAGED with basicItemBlock)
			register(DUSTY_STONE_BRICKS with basicItemBlock)
			register(DUSTY_STONE_CRACKED_BRICKS with basicItemBlock)
			
			register(OBSIDIAN_STAIRS with basicItemBlock)
			register(OBSIDIAN_FALLING with basicItemBlock)
			register(OBSIDIAN_SMOOTH with basicItemBlock)
			register(OBSIDIAN_CHISELED with basicItemBlock)
			register(OBSIDIAN_PILLAR with basicItemBlock)
			register(OBSIDIAN_SMOOTH_LIT with basicItemBlock)
			register(OBSIDIAN_CHISELED_LIT with basicItemBlock)
			register(OBSIDIAN_PILLAR_LIT with basicItemBlock)
			
			register(END_STONE_INFESTED with basicItemBlock)
			register(END_STONE_BURNED with basicItemBlock)
			register(END_STONE_ENCHANTED with basicItemBlock)
			
			register(DARK_LOAM with basicItemBlock)
			register(DARK_LOAM_SLAB with slabItemBlock(DARK_LOAM_SLAB, DARK_LOAM_DOUBLE_SLAB))
			register(DARK_LOAM_DOUBLE_SLAB)
			
			register(WHITEBARK_LOG with basicItemBlock)
			register(WHITEBARK with basicItemBlock)
			register(WHITEBARK_PLANKS with basicItemBlock)
			register(WHITEBARK_STAIRS with basicItemBlock)
			register(WHITEBARK_SLAB with woodenSlabItemBlock(WHITEBARK_SLAB, WHITEBARK_DOUBLE_SLAB))
			
			register(ENDER_GOO)
			register(INFUSED_TNT with ::ItemInfusedTNT)
			register(DARK_CHEST with basicItemBlock)
			register(LOOT_CHEST with basicItemBlock)
			
			register(END_POWDER_ORE with basicItemBlock)
			register(ENDIUM_ORE with basicItemBlock)
			register(STARDUST_ORE with basicItemBlock)
			register(IGNEOUS_ROCK_ORE with basicItemBlock)
			
			register(DEATH_FLOWER_DECAYING with metaItemBlock)
			register(DEATH_FLOWER_HEALED with basicItemBlock)
			register(DEATH_FLOWER_WITHERED with basicItemBlock)
			
			register(ANCIENT_COBWEB with ::ItemAncientCobweb)
			register(DRY_VINES with basicItemBlock)
			register(ENDERMAN_HEAD)
			
			register(END_PORTAL_INNER with basicItemBlock)
			register(END_PORTAL_FRAME with basicItemBlock)
			register(END_PORTAL_ACCEPTOR with basicItemBlock)
			register(VOID_PORTAL_INNER with metaItemBlock)
			register(VOID_PORTAL_FRAME with basicItemBlock)
			register(VOID_PORTAL_STORAGE with basicItemBlock)
			
			register(ENERGY_CLUSTER with basicItemBlock)
			register(CORRUPTED_ENERGY)
			
			register(TABLE_PEDESTAL with basicItemBlock)
			register(TABLE_BASE with metaItemBlock)
			register(ACCUMULATION_TABLE with metaItemBlock)
			
			register(SCAFFOLDING with basicItemBlock)
		}
		
		tile<TileEntityPortalInner.End>("end_portal_inner")
		tile<TileEntityPortalInner.Void>("void_portal_inner")
		tile<TileEntityEndPortalAcceptor>("end_portal_acceptor")
		tile<TileEntityVoidPortalStorage>("void_portal_storage")
		tile<TileEntityEnergyCluster>("energy_cluster")
		tile<TileEntityInfusedTNT>("infused_tnt")
		tile<TileEntityDarkChest>("dark_chest")
		tile<TileEntityLootChest>("loot_chest")
		tile<TileEntityTablePedestal>("table_pedestal")
		tile<TileEntityAccumulationTable>("accumulation_table")
		
		// vanilla modifications
		
		Blocks.END_BRICKS.setHardness(1.0F).setResistance(4.0F)
		Blocks.END_PORTAL_FRAME.creativeTab = null
		
		with(e.registry){
			register(BlockEndPortalOverride().apply { override(Blocks.END_PORTAL, newCreativeTab = null) })
			register(BlockChorusFlowerOverride().apply { override(Blocks.CHORUS_FLOWER) })
			register(BlockChorusPlantOverride().apply { override(Blocks.CHORUS_PLANT) })
			register(BlockDragonEggOverride().apply { override(Blocks.DRAGON_EGG) } with ::ItemDragonEgg)
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterItemBlocks(e: RegistryEvent.Register<Item>){
		temporaryItemBlocks.forEach(e.registry::register)
		temporaryItemBlocks.clear()
		
		// ore dictionary
		
		OreDictionary.registerOre("logWood", WHITEBARK_LOG)
		OreDictionary.registerOre("logWood", WHITEBARK) // UPDATE check new name for bark blocks
		OreDictionary.registerOre("plankWood", WHITEBARK_PLANKS)
		OreDictionary.registerOre("stairWood", WHITEBARK_STAIRS)
		OreDictionary.registerOre("slabWood", WHITEBARK_SLAB)
		
		// fire
		
		Blocks.FIRE.setFireInfo(WHITEBARK_LOG, 5, 5)
		Blocks.FIRE.setFireInfo(WHITEBARK, 5, 5)
		Blocks.FIRE.setFireInfo(WHITEBARK_PLANKS, 5, 20)
		Blocks.FIRE.setFireInfo(WHITEBARK_STAIRS, 5, 20)
		Blocks.FIRE.setFireInfo(WHITEBARK_SLAB, 5, 20)
		
		Blocks.FIRE.setFireInfo(INFUSED_TNT, 15, 100)
		
		Blocks.FIRE.setFireInfo(ANCIENT_COBWEB, 100, 300)
		Blocks.FIRE.setFireInfo(DRY_VINES, 100, 300)
	}
	
	// Utilities
	
	private val temporaryItemBlocks = mutableListOf<ItemBlock>()
	
	private fun Block.setup(registryName: String, translationKey: String = "hee.$registryName", inCreativeTab: Boolean = true){
		this.setRegistryName(HEE.ID, registryName)
		this.translationKey = translationKey
		
		if (inCreativeTab){
			this.creativeTab = ModCreativeTabs.main
		}
	}
	
	private fun Block.override(vanillaBlock: Block, newCreativeTab: CreativeTabs? = ModCreativeTabs.main){
		this.useVanillaName(vanillaBlock)
		this.creativeTab = newCreativeTab
		
		if (newCreativeTab is OrderedCreativeTab){
			newCreativeTab.registerOrder(Item.getItemFromBlock(vanillaBlock))
		}
	}
	
	private infix fun Block.with(itemBlock: ItemBlock) = apply {
		if (this.registryName!!.namespace == Resource.Vanilla.domain){
			itemBlock.useVanillaName(this)
		}
		else{
			itemBlock.registryName = this.registryName
		}
		
		temporaryItemBlocks.add(itemBlock)
		(itemBlock.creativeTab as? OrderedCreativeTab)?.registerOrder(itemBlock)
	}
	
	private infix fun <T : Block> T.with(itemBlockConstructor: (T) -> ItemBlock): Block{
		return with(itemBlockConstructor(this))
	}
	
	private inline fun <reified T : TileEntity> tile(registryName: String){
		GameRegistry.registerTileEntity(T::class.java, Resource.Custom(registryName))
	}
}
