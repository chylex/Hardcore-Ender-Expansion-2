package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.game.block.BlockBrewingStandOverride
import chylex.hee.game.block.BlockCauldronWithDragonsBreath
import chylex.hee.game.block.BlockCauldronWithGoo
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
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.block.BlockEndermanHead
import chylex.hee.game.block.BlockEndersol
import chylex.hee.game.block.BlockEndium
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.BlockEnhancedBrewingStand
import chylex.hee.game.block.BlockEternalFire
import chylex.hee.game.block.BlockExperienceGateController
import chylex.hee.game.block.BlockExperienceGateOutline
import chylex.hee.game.block.BlockFallingObsidian
import chylex.hee.game.block.BlockFlowerPotCustom
import chylex.hee.game.block.BlockFlowerPotDeathFlowerDecaying
import chylex.hee.game.block.BlockGloomrock
import chylex.hee.game.block.BlockGloomtorch
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.BlockHumus
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.BlockIgneousRockOre
import chylex.hee.game.block.BlockInfusedGlass
import chylex.hee.game.block.BlockInfusedTNT
import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.block.BlockLootChest
import chylex.hee.game.block.BlockMinersBurialAltar
import chylex.hee.game.block.BlockPillarCustom
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.BlockScaffolding
import chylex.hee.game.block.BlockSimple
import chylex.hee.game.block.BlockSimpleShaped
import chylex.hee.game.block.BlockSlabCustom
import chylex.hee.game.block.BlockSpawnerObsidianTowers
import chylex.hee.game.block.BlockStairsCustom
import chylex.hee.game.block.BlockStardustOre
import chylex.hee.game.block.BlockTableBase
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTableTile
import chylex.hee.game.block.BlockVoidPortalCrafted
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.BlockVoidPortalStorage
import chylex.hee.game.block.BlockVoidPortalStorageCrafted
import chylex.hee.game.block.BlockWallCustom
import chylex.hee.game.block.BlockWhitebarkLog
import chylex.hee.game.block.entity.TileEntityAccumulationTable
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.info.BlockBuilders.buildBrewingStand
import chylex.hee.game.block.info.BlockBuilders.buildCorruptedEnergy
import chylex.hee.game.block.info.BlockBuilders.buildDarkLoam
import chylex.hee.game.block.info.BlockBuilders.buildDustyStone
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneBricks
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneCracked
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneDamaged
import chylex.hee.game.block.info.BlockBuilders.buildEndPowderOre
import chylex.hee.game.block.info.BlockBuilders.buildEndStone
import chylex.hee.game.block.info.BlockBuilders.buildEnderSol
import chylex.hee.game.block.info.BlockBuilders.buildEndiumBlock
import chylex.hee.game.block.info.BlockBuilders.buildEndiumOre
import chylex.hee.game.block.info.BlockBuilders.buildEnergyCluster
import chylex.hee.game.block.info.BlockBuilders.buildEternalFire
import chylex.hee.game.block.info.BlockBuilders.buildEtherealLantern
import chylex.hee.game.block.info.BlockBuilders.buildExperienceGate
import chylex.hee.game.block.info.BlockBuilders.buildFlowerPot
import chylex.hee.game.block.info.BlockBuilders.buildGloomrock
import chylex.hee.game.block.info.BlockBuilders.buildGloomrockBricks
import chylex.hee.game.block.info.BlockBuilders.buildGloomrockSmooth
import chylex.hee.game.block.info.BlockBuilders.buildGloomtorch
import chylex.hee.game.block.info.BlockBuilders.buildGraveDirt
import chylex.hee.game.block.info.BlockBuilders.buildHumus
import chylex.hee.game.block.info.BlockBuilders.buildIgneousPlate
import chylex.hee.game.block.info.BlockBuilders.buildIgneousRockOre
import chylex.hee.game.block.info.BlockBuilders.buildInfusedGlass
import chylex.hee.game.block.info.BlockBuilders.buildJarODust
import chylex.hee.game.block.info.BlockBuilders.buildLootChest
import chylex.hee.game.block.info.BlockBuilders.buildMinersBurial
import chylex.hee.game.block.info.BlockBuilders.buildMinersBurialAltar
import chylex.hee.game.block.info.BlockBuilders.buildObsidian
import chylex.hee.game.block.info.BlockBuilders.buildObsidianVariation
import chylex.hee.game.block.info.BlockBuilders.buildObsidianVariationLit
import chylex.hee.game.block.info.BlockBuilders.buildPortalFrame
import chylex.hee.game.block.info.BlockBuilders.buildPortalFrameCrafted
import chylex.hee.game.block.info.BlockBuilders.buildPortalInner
import chylex.hee.game.block.info.BlockBuilders.buildPuzzleLogic
import chylex.hee.game.block.info.BlockBuilders.buildPuzzleWall
import chylex.hee.game.block.info.BlockBuilders.buildScaffolding
import chylex.hee.game.block.info.BlockBuilders.buildSpawnerObsidianTowers
import chylex.hee.game.block.info.BlockBuilders.buildStardustOre
import chylex.hee.game.block.info.BlockBuilders.buildTable
import chylex.hee.game.block.info.BlockBuilders.buildTablePedestal
import chylex.hee.game.block.info.BlockBuilders.buildVantablock
import chylex.hee.game.block.info.BlockBuilders.buildWhitebark
import chylex.hee.game.block.info.BlockBuilders.buildWhitebarkPlanks
import chylex.hee.game.item.ItemAncientCobweb
import chylex.hee.game.item.ItemBlockPlant
import chylex.hee.game.item.ItemBlockSlabFuel
import chylex.hee.game.item.ItemBlockWithMetadata
import chylex.hee.game.item.ItemDragonEgg
import chylex.hee.game.item.ItemInfusedTNT
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.creativeTabIn
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.useVanillaName
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSlab
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

@SubscribeAllEvents(modid = HEE.ID)
object ModBlocks{
	init{
		FluidRegistry.registerFluid(FluidEnderGoo)
		FluidRegistry.registerFluid(FluidEnderGooPurified)
	}
	
	// Blocks: Building (Uncategorized)
	
	@JvmField val ETHEREAL_LANTERN = BlockSimple(buildEtherealLantern).apply { setup("ethereal_lantern") }
	@JvmField val STONE_BRICK_WALL = BlockWallCustom(Blocks.STONEBRICK).apply { setup("stone_brick_wall") }
	@JvmField val INFUSED_GLASS    = BlockInfusedGlass(buildInfusedGlass).apply { setup("infused_glass") }
	@JvmField val VANTABLOCK       = BlockSimple(buildVantablock).apply { setup("vantablock") }
	@JvmField val ENDIUM_BLOCK     = BlockEndium(buildEndiumBlock).apply { setup("endium_block") }
	@JvmField val ENDERSOL         = BlockEndersol(buildEnderSol).apply { setup("endersol") }
	@JvmField val HUMUS            = BlockHumus(buildHumus).apply { setup("humus") }
	
	// Blocks: Building (Gloomrock)
	
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
	
	@JvmField val DUSTY_STONE                   = BlockDustyStonePlain(buildDustyStone).apply { setup("dusty_stone") }
	@JvmField val DUSTY_STONE_CRACKED           = BlockDustyStonePlain(buildDustyStoneCracked).apply { setup("dusty_stone_cracked") }
	@JvmField val DUSTY_STONE_DAMAGED           = BlockDustyStonePlain(buildDustyStoneDamaged).apply { setup("dusty_stone_damaged") }
	@JvmField val DUSTY_STONE_BRICKS            = BlockDustyStoneBricks(buildDustyStoneBricks).apply { setup("dusty_stone_bricks") } // UPDATE: update recipe json to include tag to allow all dusty stone variations
	@JvmField val DUSTY_STONE_CRACKED_BRICKS    = BlockDustyStoneBricks(buildDustyStoneBricks).apply { setup("dusty_stone_cracked_bricks") }
	@JvmField val DUSTY_STONE_BRICK_STAIRS      = BlockStairsCustom(DUSTY_STONE_BRICKS).apply { setup("dusty_stone_brick_stairs") }
	@JvmField val DUSTY_STONE_BRICK_SLAB        = BlockSlabCustom.Half(buildDustyStoneBricks).apply { setup("dusty_stone_brick_slab") }
	@JvmField val DUSTY_STONE_BRICK_DOUBLE_SLAB = BlockSlabCustom.Full(buildDustyStoneBricks, DUSTY_STONE_BRICK_SLAB).apply { setup("dusty_stone_brick_slab_double", "hee.dusty_stone_brick_slab") }
	
	// Blocks: Building (Obsidian)
	
	@JvmField val OBSIDIAN_STAIRS       = BlockStairsCustom(Blocks.OBSIDIAN).apply { setup("obsidian_stairs") }
	@JvmField val OBSIDIAN_FALLING      = BlockFallingObsidian(buildObsidian).apply { setup("obsidian_falling") }
	@JvmField val OBSIDIAN_SMOOTH       = BlockSimple(buildObsidianVariation).apply { setup("obsidian_smooth") }
	@JvmField val OBSIDIAN_CHISELED     = BlockSimple(buildObsidianVariation).apply { setup("obsidian_chiseled") }
	@JvmField val OBSIDIAN_PILLAR       = BlockPillarCustom(buildObsidianVariation).apply { setup("obsidian_pillar") }
	@JvmField val OBSIDIAN_SMOOTH_LIT   = BlockSimple(buildObsidianVariationLit).apply { setup("obsidian_smooth_lit") }
	@JvmField val OBSIDIAN_CHISELED_LIT = BlockSimple(buildObsidianVariationLit).apply { setup("obsidian_chiseled_lit") }
	@JvmField val OBSIDIAN_PILLAR_LIT   = BlockPillarCustom(buildObsidianVariationLit).apply { setup("obsidian_pillar_lit") }
	
	// Blocks: Building (End Stone)
	
	@JvmField val END_STONE_INFESTED  = BlockSimple(buildEndStone).apply { setup("end_stone_infested") }
	@JvmField val END_STONE_BURNED    = BlockSimple(buildEndStone).apply { setup("end_stone_burned") }
	@JvmField val END_STONE_ENCHANTED = BlockSimple(buildEndStone).apply { setup("end_stone_enchanted") }
	
	// Blocks: Building (Dark Loam)
	
	@JvmField val DARK_LOAM             = BlockSimple(buildDarkLoam).apply { setup("dark_loam") }
	@JvmField val DARK_LOAM_SLAB        = BlockSlabCustom.Half(buildDarkLoam).apply { setup("dark_loam_slab") }
	@JvmField val DARK_LOAM_DOUBLE_SLAB = BlockSlabCustom.Full(buildDarkLoam, DARK_LOAM_SLAB).apply { setup("dark_loam_slab_double", "hee.dark_loam_slab") }
	
	// Blocks: Building (Grave Dirt)
	
	@JvmField val GRAVE_DIRT_PLAIN      = BlockGraveDirt(buildGraveDirt).apply { setup("grave_dirt") }
	@JvmField val GRAVE_DIRT_LOOT       = BlockGraveDirt.Loot(buildGraveDirt).apply { setup("grave_dirt_loot") }
	@JvmField val GRAVE_DIRT_SPIDERLING = BlockGraveDirt.Spiderling(buildGraveDirt).apply { setup("grave_dirt_spiderling") }
	
	// Blocks: Building (Wood)
	
	@JvmField val WHITEBARK_LOG         = BlockWhitebarkLog().apply { setup("whitebark_log") }
	@JvmField val WHITEBARK             = BlockSimple(buildWhitebark).apply { setup("whitebark") }
	@JvmField val WHITEBARK_PLANKS      = BlockSimple(buildWhitebarkPlanks).apply { setup("whitebark_planks") }
	@JvmField val WHITEBARK_STAIRS      = BlockStairsCustom(WHITEBARK_PLANKS).apply { setup("whitebark_stairs") }
	@JvmField val WHITEBARK_SLAB        = BlockSlabCustom.Half(buildWhitebarkPlanks).apply { setup("whitebark_slab") }
	@JvmField val WHITEBARK_DOUBLE_SLAB = BlockSlabCustom.Full(buildWhitebarkPlanks, WHITEBARK_SLAB).apply { setup("whitebark_slab_double", "hee.whitebark_slab") }
	
	// Blocks: Building (Miner's Burial)
	
	@JvmField val MINERS_BURIAL_BLOCK_PLAIN    = BlockSimple(buildMinersBurial).apply { setup("miners_burial_block_plain") }
	@JvmField val MINERS_BURIAL_BLOCK_CHISELED = BlockSimple(buildMinersBurial).apply { setup("miners_burial_block_chiseled") }
	@JvmField val MINERS_BURIAL_BLOCK_PILLAR   = BlockPillarCustom(buildMinersBurial).apply { setup("miners_burial_block_pillar") }
	@JvmField val MINERS_BURIAL_BLOCK_JAIL     = BlockSimple(buildMinersBurial).apply { setup("miners_burial_block_jail") }
	@JvmField val MINERS_BURIAL_ALTAR          = BlockMinersBurialAltar(buildMinersBurialAltar).apply { setup("miners_burial_altar") }
	
	// Blocks: Fluids
	
	@JvmField val ENDER_GOO          = BlockEnderGoo().apply { setup("ender_goo") }
	@JvmField val PURIFIED_ENDER_GOO = BlockEnderGooPurified().apply { setup("purified_ender_goo") }
	
	@JvmField val CAULDRON_ENDER_GOO          = BlockCauldronWithGoo(ENDER_GOO).apply { setup("cauldron_ender_goo", inCreativeTab = false) }
	@JvmField val CAULDRON_PURIFIED_ENDER_GOO = BlockCauldronWithGoo(PURIFIED_ENDER_GOO).apply { setup("cauldron_purified_ender_goo", inCreativeTab = false) }
	@JvmField val CAULDRON_DRAGONS_BREATH     = BlockCauldronWithDragonsBreath().apply { setup("cauldron_dragons_breath", inCreativeTab = false) }
	
	// Blocks: Interactive (Storage)
	
	@JvmField val JAR_O_DUST = BlockJarODust(buildJarODust).apply { setup("jar_o_dust") }
	@JvmField val DARK_CHEST = BlockDarkChest(buildGloomrock).apply { setup("dark_chest") } // UPDATE: update recipe json to include tag to allow either slab variation
	@JvmField val LOOT_CHEST = BlockLootChest(buildLootChest).apply { setup("loot_chest") }
	
	// Blocks: Interactive (Puzzle)
	
	@JvmField val PUZZLE_WALL       = BlockSimple(buildPuzzleWall).apply { setup("puzzle_block_wall") }
	@JvmField val PUZZLE_PLAIN      = BlockPuzzleLogic.Plain(buildPuzzleLogic).apply { setup("puzzle_block_plain") }
	@JvmField val PUZZLE_BURST_3    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 1).apply { setup("puzzle_block_burst_3") }
	@JvmField val PUZZLE_BURST_5    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 2).apply { setup("puzzle_block_burst_5") }
	@JvmField val PUZZLE_REDIRECT_1 = BlockPuzzleLogic.Redirect(buildPuzzleLogic, arrayOf(NORTH)).apply { setup("puzzle_block_redirect_1") }
	@JvmField val PUZZLE_REDIRECT_2 = BlockPuzzleLogic.Redirect(buildPuzzleLogic, arrayOf(NORTH, SOUTH)).apply { setup("puzzle_block_redirect_2") }
	@JvmField val PUZZLE_REDIRECT_4 = BlockPuzzleLogic.RedirectAll(buildPuzzleLogic).apply { setup("puzzle_block_redirect_4") }
	@JvmField val PUZZLE_TELEPORT   = BlockPuzzleLogic.Teleport(buildPuzzleLogic).apply { setup("puzzle_block_teleport") }
	
	// Blocks: Interactive (Gates)
	
	@JvmField val EXPERIENCE_GATE            = BlockExperienceGateOutline(buildExperienceGate).apply { setup("experience_gate") }
	@JvmField val EXPERIENCE_GATE_CONTROLLER = BlockExperienceGateController(buildExperienceGate).apply { setup("experience_gate_controller") }
	
	// Blocks: Interactive (Uncategorized)
	
	@JvmField val INFUSED_TNT            = BlockInfusedTNT().apply { setup("infused_tnt", translationKey = "tnt", inCreativeTab = false) }
	@JvmField val IGNEOUS_PLATE          = BlockIgneousPlate(buildIgneousPlate).apply { setup("igneous_plate") }
	@JvmField val ENHANCED_BREWING_STAND = BlockEnhancedBrewingStand(buildBrewingStand).apply { setup("enhanced_brewing_stand") }
	
	// Blocks: Ores
	
	@JvmField val END_POWDER_ORE   = BlockEndPowderOre(buildEndPowderOre).apply { setup("end_powder_ore") }
	@JvmField val ENDIUM_ORE       = BlockEndium(buildEndiumOre).apply { setup("endium_ore") }
	@JvmField val STARDUST_ORE     = BlockStardustOre(buildStardustOre).apply { setup("stardust_ore") }
	@JvmField val IGNEOUS_ROCK_ORE = BlockIgneousRockOre(buildIgneousRockOre).apply { setup("igneous_rock_ore") }
	
	// Blocks: Decorative (Plants)
	
	@JvmField val DEATH_FLOWER_DECAYING = BlockDeathFlowerDecaying().apply { setup("death_flower") }
	@JvmField val DEATH_FLOWER_HEALED   = BlockEndPlant().apply { setup("death_flower_healed") }
	@JvmField val DEATH_FLOWER_WITHERED = BlockEndPlant().apply { setup("death_flower_withered") }
	
	@JvmField val POTTED_DEATH_FLOWER_DECAYING = BlockFlowerPotDeathFlowerDecaying(buildFlowerPot, DEATH_FLOWER_DECAYING).apply { setup("potted_death_flower", translationKey = "flowerPot", inCreativeTab = false) }
	@JvmField val POTTED_DEATH_FLOWER_HEALED   = BlockFlowerPotCustom(buildFlowerPot, DEATH_FLOWER_HEALED).apply { setup("potted_death_flower_healed", translationKey = "flowerPot", inCreativeTab = false) }
	@JvmField val POTTED_DEATH_FLOWER_WITHERED = BlockFlowerPotCustom(buildFlowerPot, DEATH_FLOWER_WITHERED).apply { setup("potted_death_flower_withered", translationKey = "flowerPot", inCreativeTab = false) }
	
	// Blocks: Decorative (Uncategorized)
	
	@JvmField val ANCIENT_COBWEB = BlockAncientCobweb().apply { setup("ancient_cobweb") }
	@JvmField val DRY_VINES      = BlockDryVines().apply { setup("dry_vines") }
	@JvmField val ENDERMAN_HEAD  = BlockEndermanHead().apply { setup("enderman_head_block", inCreativeTab = false) }
	
	// Blocks: Spawners
	
	@JvmField val SPAWNER_OBSIDIAN_TOWERS = BlockSpawnerObsidianTowers(buildSpawnerObsidianTowers).apply { setup("spawner_obsidian_towers", inCreativeTab = false) }
	
	// Blocks: Portals
	
	private val portalFrameAABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0)
	
	@JvmField val END_PORTAL_INNER    = BlockEndPortalInner(buildPortalInner).apply { setup("end_portal_inner", inCreativeTab = false) }
	@JvmField val END_PORTAL_FRAME    = BlockSimpleShaped(buildPortalFrame, portalFrameAABB).apply { setup("end_portal_frame") }
	@JvmField val END_PORTAL_ACCEPTOR = BlockEndPortalAcceptor(buildPortalFrame, portalFrameAABB).apply { setup("end_portal_acceptor") }
	
	@JvmField val VOID_PORTAL_INNER   = BlockVoidPortalInner(buildPortalInner).apply { setup("void_portal_inner", inCreativeTab = false) }
	@JvmField val VOID_PORTAL_FRAME   = BlockSimpleShaped(buildPortalFrame, portalFrameAABB).apply { setup("void_portal_frame") }
	@JvmField val VOID_PORTAL_STORAGE = BlockVoidPortalStorage(buildPortalFrame, portalFrameAABB).apply { setup("void_portal_storage") }
	
	@JvmField val VOID_PORTAL_FRAME_CRAFTED   = BlockVoidPortalCrafted(buildPortalFrameCrafted, portalFrameAABB).apply { setup("void_portal_frame_crafted", translationKey = "hee.void_portal_frame", inCreativeTab = false) }
	@JvmField val VOID_PORTAL_STORAGE_CRAFTED = BlockVoidPortalStorageCrafted(buildPortalFrameCrafted, portalFrameAABB).apply { setup("void_portal_storage_crafted", translationKey = "hee.void_portal_storage", inCreativeTab = false) }
	
	// Blocks: Energy
	
	@JvmField val ENERGY_CLUSTER   = BlockEnergyCluster(buildEnergyCluster).apply { setup("energy_cluster") }
	@JvmField val CORRUPTED_ENERGY = BlockCorruptedEnergy(buildCorruptedEnergy).apply { setup("corrupted_energy") }
	
	// Blocks: Tables
	
	@JvmField val TABLE_PEDESTAL     = BlockTablePedestal(buildTablePedestal).apply { setup("table_pedestal") } // UPDATE: update recipe json to include tag to allow all gloomrock variations
	@JvmField val TABLE_BASE         = BlockTableBase(buildTable).apply { setup("table_base") }
	@JvmField val ACCUMULATION_TABLE = BlockTableTile(buildTable, ::TileEntityAccumulationTable, minAllowedTier = 1).apply { setup("accumulation_table") }
	
	// Blocks: Utilities
	
	@JvmField val ETERNAL_FIRE = BlockEternalFire(buildEternalFire).apply { setup("eternal_fire", inCreativeTab = false) }
	@JvmField val SCAFFOLDING  = BlockScaffolding(buildScaffolding).apply { setup("scaffolding") }
	
	// Registry
	
	private val basicItemBlock = ::ItemBlock
	private val metaItemBlock = ::ItemBlockWithMetadata
	
	private fun slabItemBlock(half: BlockSlabCustom.Half, full: BlockSlabCustom.Full): ItemBlock{
		return ItemSlab(half, half, full).apply { hasSubtypes = false }
	}
	
	private fun woodenSlabItemBlock(half: BlockSlabCustom.Half, full: BlockSlabCustom.Full): ItemBlock{
		return ItemBlockSlabFuel(half, full, burnTicks = 150).apply { hasSubtypes = false }
	}
	
	private fun plantItemBlock(potted: BlockFlowerPotCustom): (Block) -> ItemBlock{
		return { block -> ItemBlockPlant(block, potted) }
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
			register(DUSTY_STONE_BRICK_STAIRS with basicItemBlock)
			register(DUSTY_STONE_BRICK_SLAB with slabItemBlock(DUSTY_STONE_BRICK_SLAB, DUSTY_STONE_BRICK_DOUBLE_SLAB))
			register(DUSTY_STONE_BRICK_DOUBLE_SLAB)
			
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
			
			register(GRAVE_DIRT_PLAIN with basicItemBlock)
			register(GRAVE_DIRT_LOOT with basicItemBlock)
			register(GRAVE_DIRT_SPIDERLING with basicItemBlock)
			
			register(WHITEBARK_LOG with basicItemBlock)
			register(WHITEBARK with basicItemBlock)
			register(WHITEBARK_PLANKS with basicItemBlock)
			register(WHITEBARK_STAIRS with basicItemBlock)
			register(WHITEBARK_SLAB with woodenSlabItemBlock(WHITEBARK_SLAB, WHITEBARK_DOUBLE_SLAB))
			register(WHITEBARK_DOUBLE_SLAB)
			
			register(MINERS_BURIAL_BLOCK_PLAIN with basicItemBlock)
			register(MINERS_BURIAL_BLOCK_CHISELED with basicItemBlock)
			register(MINERS_BURIAL_BLOCK_PILLAR with basicItemBlock)
			register(MINERS_BURIAL_BLOCK_JAIL with basicItemBlock)
			register(MINERS_BURIAL_ALTAR with basicItemBlock)
			
			register(ENDER_GOO)
			register(PURIFIED_ENDER_GOO)
			register(CAULDRON_ENDER_GOO)
			register(CAULDRON_PURIFIED_ENDER_GOO)
			register(CAULDRON_DRAGONS_BREATH)
			
			register(JAR_O_DUST with basicItemBlock)
			register(DARK_CHEST with basicItemBlock)
			register(LOOT_CHEST with basicItemBlock)
			
			register(PUZZLE_WALL with basicItemBlock)
			register(PUZZLE_PLAIN with basicItemBlock)
			register(PUZZLE_BURST_3 with basicItemBlock)
			register(PUZZLE_BURST_5 with basicItemBlock)
			register(PUZZLE_REDIRECT_1 with basicItemBlock)
			register(PUZZLE_REDIRECT_2 with basicItemBlock)
			register(PUZZLE_REDIRECT_4 with basicItemBlock)
			register(PUZZLE_TELEPORT with basicItemBlock)
			
			register(INFUSED_TNT with ::ItemInfusedTNT)
			register(IGNEOUS_PLATE with basicItemBlock)
			register(ENHANCED_BREWING_STAND with basicItemBlock)
			register(EXPERIENCE_GATE with basicItemBlock)
			register(EXPERIENCE_GATE_CONTROLLER)
			
			register(END_POWDER_ORE with basicItemBlock)
			register(ENDIUM_ORE with basicItemBlock)
			register(STARDUST_ORE with basicItemBlock)
			register(IGNEOUS_ROCK_ORE with basicItemBlock)
			
			register(DEATH_FLOWER_DECAYING with plantItemBlock(POTTED_DEATH_FLOWER_DECAYING))
			register(DEATH_FLOWER_HEALED with plantItemBlock(POTTED_DEATH_FLOWER_HEALED))
			register(DEATH_FLOWER_WITHERED with plantItemBlock(POTTED_DEATH_FLOWER_WITHERED))
			register(POTTED_DEATH_FLOWER_DECAYING)
			register(POTTED_DEATH_FLOWER_HEALED)
			register(POTTED_DEATH_FLOWER_WITHERED)
			
			register(ANCIENT_COBWEB with ::ItemAncientCobweb)
			register(DRY_VINES with basicItemBlock)
			register(ENDERMAN_HEAD)
			
			register(SPAWNER_OBSIDIAN_TOWERS)
			
			register(END_PORTAL_INNER with basicItemBlock)
			register(END_PORTAL_FRAME with basicItemBlock)
			register(END_PORTAL_ACCEPTOR with basicItemBlock)
			register(VOID_PORTAL_INNER with metaItemBlock)
			register(VOID_PORTAL_FRAME with basicItemBlock)
			register(VOID_PORTAL_STORAGE with basicItemBlock)
			register(VOID_PORTAL_FRAME_CRAFTED with basicItemBlock)
			register(VOID_PORTAL_STORAGE_CRAFTED with basicItemBlock)
			
			register(ENERGY_CLUSTER with basicItemBlock)
			register(CORRUPTED_ENERGY)
			
			register(TABLE_PEDESTAL with basicItemBlock)
			register(TABLE_BASE with metaItemBlock)
			register(ACCUMULATION_TABLE with metaItemBlock)
			
			register(ETERNAL_FIRE)
			register(SCAFFOLDING with basicItemBlock)
		}
		
		tile<TileEntityAccumulationTable>("accumulation_table")
		tile<TileEntityBrewingStandCustom>("brewing_stand")
		tile<TileEntityDarkChest>("dark_chest")
		tile<TileEntityEndPortalAcceptor>("end_portal_acceptor")
		tile<TileEntityEnergyCluster>("energy_cluster")
		tile<TileEntityExperienceGate>("experience_gate")
		tile<TileEntityIgneousPlate>("igneous_plate")
		tile<TileEntityInfusedTNT>("infused_tnt")
		tile<TileEntityJarODust>("jar_o_dust")
		tile<TileEntityLootChest>("loot_chest")
		tile<TileEntityMinersBurialAltar>("miners_burial_altar")
		tile<TileEntityPortalInner.End>("end_portal_inner")
		tile<TileEntityPortalInner.Void>("void_portal_inner")
		tile<TileEntitySpawnerObsidianTower>("spawner_obsidian_tower")
		tile<TileEntityTablePedestal>("table_pedestal")
		tile<TileEntityVoidPortalStorage>("void_portal_storage")
		
		// vanilla modifications
		
		Blocks.END_BRICKS.setHardness(1.0F).setResistance(4.0F)
		Blocks.END_PORTAL_FRAME.creativeTabIn = null
		
		with(e.registry){
			register(BlockEndPortalOverride().apply { override(Blocks.END_PORTAL, newCreativeTab = null) })
			register(BlockBrewingStandOverride(buildBrewingStand).apply { override(Blocks.BREWING_STAND) })
			register(BlockChorusPlantOverride().apply { override(Blocks.CHORUS_PLANT) })
			register(BlockDragonEggOverride().apply { override(Blocks.DRAGON_EGG) } with ::ItemDragonEgg)
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterItemBlocks(e: RegistryEvent.Register<Item>){
		temporaryItemBlocks.forEach(e.registry::register)
		temporaryItemBlocks.clear()
		
		Item.getItemFromBlock(JAR_O_DUST).setMaxStackSize(1)
		
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
		
		// UPDATE hardcoded shit in BlockFire Blocks.FIRE.setFireInfo(INFUSED_TNT, 15, 100)
		
		Blocks.FIRE.setFireInfo(ANCIENT_COBWEB, 100, 300)
		Blocks.FIRE.setFireInfo(DRY_VINES, 100, 300)
	}
	
	// Utilities
	
	private val temporaryItemBlocks = mutableListOf<ItemBlock>()
	
	private fun Block.setup(registryName: String, translationKey: String = "hee.$registryName", inCreativeTab: Boolean = true){
		this.setRegistryName(HEE.ID, registryName)
		this.translationKey = translationKey
		
		if (inCreativeTab){
			this.creativeTabIn = ModCreativeTabs.main
		}
		
		if (this.translucent || this.lightOpacity == 0){
			this.useNeighborBrightness = true
		}
	}
	
	private fun Block.override(vanillaBlock: Block, newCreativeTab: CreativeTabs? = ModCreativeTabs.main){
		this.useVanillaName(vanillaBlock)
		this.creativeTabIn = newCreativeTab
		
		if (newCreativeTab is OrderedCreativeTab){
			newCreativeTab.registerOrder(Item.getItemFromBlock(vanillaBlock))
		}
	}
	
	private infix fun Block.with(itemBlock: ItemBlock) = apply {
		if (Resource.isVanilla(this.registryName!!)){
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
