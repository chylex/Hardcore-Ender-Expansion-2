package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.game.block.BlockBrewingStandCustom
import chylex.hee.game.block.BlockCauldronWithDragonsBreath
import chylex.hee.game.block.BlockCauldronWithGoo
import chylex.hee.game.block.BlockCorruptedEnergy
import chylex.hee.game.block.BlockDarkChest
import chylex.hee.game.block.BlockDeathFlower
import chylex.hee.game.block.BlockDeathFlowerDecaying
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockDustyStoneBricks
import chylex.hee.game.block.BlockDustyStoneUnstable
import chylex.hee.game.block.BlockEndPortalAcceptor
import chylex.hee.game.block.BlockEndPortalInner
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.block.BlockEndPowderOre
import chylex.hee.game.block.BlockEndStoneCustom
import chylex.hee.game.block.BlockEnderGoo
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.block.BlockEndersol
import chylex.hee.game.block.BlockEndium
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.BlockEternalFire
import chylex.hee.game.block.BlockExperienceGateController
import chylex.hee.game.block.BlockExperienceGateOutline
import chylex.hee.game.block.BlockFallingObsidian
import chylex.hee.game.block.BlockFlammableSlab
import chylex.hee.game.block.BlockFlammableStairs
import chylex.hee.game.block.BlockFlowerPotCustom
import chylex.hee.game.block.BlockFlowerPotDeathFlower
import chylex.hee.game.block.BlockFlowerPotDeathFlowerDecaying
import chylex.hee.game.block.BlockGloomrock
import chylex.hee.game.block.BlockGloomrockSmooth
import chylex.hee.game.block.BlockGloomrockSmoothColored
import chylex.hee.game.block.BlockGloomrockSmoothSlab
import chylex.hee.game.block.BlockGloomrockSmoothStairs
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
import chylex.hee.game.block.BlockMinersBurialCube
import chylex.hee.game.block.BlockMinersBurialPillar
import chylex.hee.game.block.BlockObsidianCube
import chylex.hee.game.block.BlockObsidianPillar
import chylex.hee.game.block.BlockPortalFrame
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.BlockPuzzleWall
import chylex.hee.game.block.BlockScaffolding
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.BlockSkullCustom
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
import chylex.hee.game.block.BlockWhitebark
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.block.BlockWhitebarkLog
import chylex.hee.game.block.BlockWhitebarkPlanks
import chylex.hee.game.block.BlockWhitebarkSapling
import chylex.hee.game.block.HeeBlock
import chylex.hee.game.block.entity.TileEntityAccumulationTable
import chylex.hee.game.block.entity.TileEntityExperienceTable
import chylex.hee.game.block.entity.TileEntityInfusionTable
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.properties.BlockBuilders.buildAncientCobweb
import chylex.hee.game.block.properties.BlockBuilders.buildBrewingStand
import chylex.hee.game.block.properties.BlockBuilders.buildCauldron
import chylex.hee.game.block.properties.BlockBuilders.buildCorruptedEnergy
import chylex.hee.game.block.properties.BlockBuilders.buildDarkLoam
import chylex.hee.game.block.properties.BlockBuilders.buildDragonEgg
import chylex.hee.game.block.properties.BlockBuilders.buildDryVines
import chylex.hee.game.block.properties.BlockBuilders.buildDustyStone
import chylex.hee.game.block.properties.BlockBuilders.buildDustyStoneBricks
import chylex.hee.game.block.properties.BlockBuilders.buildDustyStoneCracked
import chylex.hee.game.block.properties.BlockBuilders.buildDustyStoneDamaged
import chylex.hee.game.block.properties.BlockBuilders.buildEndPortalOverride
import chylex.hee.game.block.properties.BlockBuilders.buildEndPowderOre
import chylex.hee.game.block.properties.BlockBuilders.buildEndStone
import chylex.hee.game.block.properties.BlockBuilders.buildEnderSol
import chylex.hee.game.block.properties.BlockBuilders.buildEndermanHead
import chylex.hee.game.block.properties.BlockBuilders.buildEndiumBlock
import chylex.hee.game.block.properties.BlockBuilders.buildEndiumOre
import chylex.hee.game.block.properties.BlockBuilders.buildEnergyCluster
import chylex.hee.game.block.properties.BlockBuilders.buildEternalFire
import chylex.hee.game.block.properties.BlockBuilders.buildEtherealLantern
import chylex.hee.game.block.properties.BlockBuilders.buildExperienceGate
import chylex.hee.game.block.properties.BlockBuilders.buildFlowerPot
import chylex.hee.game.block.properties.BlockBuilders.buildGloomrock
import chylex.hee.game.block.properties.BlockBuilders.buildGloomrockBricks
import chylex.hee.game.block.properties.BlockBuilders.buildGloomrockSmooth
import chylex.hee.game.block.properties.BlockBuilders.buildGloomtorch
import chylex.hee.game.block.properties.BlockBuilders.buildGraveDirt
import chylex.hee.game.block.properties.BlockBuilders.buildHumus
import chylex.hee.game.block.properties.BlockBuilders.buildIgneousPlate
import chylex.hee.game.block.properties.BlockBuilders.buildIgneousRockOre
import chylex.hee.game.block.properties.BlockBuilders.buildInfusedGlass
import chylex.hee.game.block.properties.BlockBuilders.buildJarODust
import chylex.hee.game.block.properties.BlockBuilders.buildLootChest
import chylex.hee.game.block.properties.BlockBuilders.buildMinersBurial
import chylex.hee.game.block.properties.BlockBuilders.buildMinersBurialIndestructible
import chylex.hee.game.block.properties.BlockBuilders.buildObsidian
import chylex.hee.game.block.properties.BlockBuilders.buildObsidianTowerTop
import chylex.hee.game.block.properties.BlockBuilders.buildObsidianVariation
import chylex.hee.game.block.properties.BlockBuilders.buildObsidianVariationLit
import chylex.hee.game.block.properties.BlockBuilders.buildPlant
import chylex.hee.game.block.properties.BlockBuilders.buildPortalFrame
import chylex.hee.game.block.properties.BlockBuilders.buildPortalFrameCrafted
import chylex.hee.game.block.properties.BlockBuilders.buildPortalInner
import chylex.hee.game.block.properties.BlockBuilders.buildPuzzleLogic
import chylex.hee.game.block.properties.BlockBuilders.buildPuzzleWall
import chylex.hee.game.block.properties.BlockBuilders.buildScaffolding
import chylex.hee.game.block.properties.BlockBuilders.buildSpawnerObsidianTowers
import chylex.hee.game.block.properties.BlockBuilders.buildStardustOre
import chylex.hee.game.block.properties.BlockBuilders.buildTable
import chylex.hee.game.block.properties.BlockBuilders.buildTablePedestal
import chylex.hee.game.block.properties.BlockBuilders.buildVantablock
import chylex.hee.game.block.properties.BlockBuilders.buildWhitebark
import chylex.hee.game.block.properties.BlockBuilders.buildWhitebarkLeaves
import chylex.hee.game.block.properties.BlockBuilders.buildWhitebarkPlanks
import chylex.hee.game.block.properties.BlockBuilders.buildWhitebarkSapling
import chylex.hee.game.block.properties.CustomSkull
import chylex.hee.game.item.ItemAncientCobweb
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.game.item.ItemDragonEgg
import chylex.hee.game.item.ItemInfusedTNT
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.builder.HeeBlockItemBuilder
import chylex.hee.game.world.generation.feature.basic.AutumnTreeGenerator
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.getRegistryEntries
import chylex.hee.system.named
import chylex.hee.system.useVanillaName
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.material.MaterialColor
import net.minecraft.fluid.Fluid
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModBlocks {
	val ALL
		get() = getRegistryEntries<Block>(this) + overrideBlocks
	
	val FLUIDS
		get() = listOf(FluidEnderGoo, FluidEnderGooPurified)
	
	// Blocks: Building (Uncategorized)
	
	@JvmField val ETHEREAL_LANTERN = HeeBlock(buildEtherealLantern) named "ethereal_lantern"
	@JvmField val STONE_BRICK_WALL = BlockWallCustom(Blocks.STONE_BRICKS) named "stone_brick_wall"
	@JvmField val INFUSED_GLASS    = BlockInfusedGlass(buildInfusedGlass) named "infused_glass"
	@JvmField val VANTABLOCK       = HeeBlock(buildVantablock) named "vantablock"
	@JvmField val ENDIUM_BLOCK     = BlockEndium.Block(buildEndiumBlock) named "endium_block"
	@JvmField val ENDERSOL         = BlockEndersol(buildEnderSol, mergeBottom = Blocks.END_STONE) named "endersol"
	@JvmField val HUMUS            = BlockHumus(buildHumus, mergeBottom = ENDERSOL) named "humus"
	
	// Blocks: Building (Gloomrock)
	
	@JvmField val GLOOMROCK                = BlockGloomrock(buildGloomrock) named "gloomrock"
	@JvmField val GLOOMROCK_BRICKS         = BlockGloomrock(buildGloomrockBricks) named "gloomrock_bricks"
	@JvmField val GLOOMROCK_BRICK_STAIRS   = BlockStairsCustom(GLOOMROCK_BRICKS) named "gloomrock_brick_stairs"
	@JvmField val GLOOMROCK_BRICK_SLAB     = BlockSlabCustom(GLOOMROCK_BRICKS) named "gloomrock_brick_slab"
	@JvmField val GLOOMROCK_SMOOTH         = BlockGloomrockSmooth(buildGloomrockSmooth) named "gloomrock_smooth"
	@JvmField val GLOOMROCK_SMOOTH_STAIRS  = BlockGloomrockSmoothStairs(GLOOMROCK_SMOOTH) named "gloomrock_smooth_stairs"
	@JvmField val GLOOMROCK_SMOOTH_SLAB    = BlockGloomrockSmoothSlab(GLOOMROCK_SMOOTH) named "gloomrock_smooth_slab"
	@JvmField val GLOOMROCK_SMOOTH_RED     = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_red"
	@JvmField val GLOOMROCK_SMOOTH_ORANGE  = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_orange"
	@JvmField val GLOOMROCK_SMOOTH_YELLOW  = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_yellow"
	@JvmField val GLOOMROCK_SMOOTH_GREEN   = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_green"
	@JvmField val GLOOMROCK_SMOOTH_CYAN    = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_cyan"
	@JvmField val GLOOMROCK_SMOOTH_BLUE    = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_blue"
	@JvmField val GLOOMROCK_SMOOTH_PURPLE  = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_purple"
	@JvmField val GLOOMROCK_SMOOTH_MAGENTA = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_magenta"
	@JvmField val GLOOMROCK_SMOOTH_WHITE   = BlockGloomrockSmoothColored(buildGloomrockSmooth) named "gloomrock_smooth_white"
	@JvmField val GLOOMTORCH               = BlockGloomtorch(buildGloomtorch) named "gloomtorch"
	
	// Blocks: Building (Dusty Stone)
	
	@JvmField val DUSTY_STONE                = BlockDustyStoneUnstable(buildDustyStone) named "dusty_stone"
	@JvmField val DUSTY_STONE_CRACKED        = BlockDustyStoneUnstable(buildDustyStoneCracked) named "dusty_stone_cracked"
	@JvmField val DUSTY_STONE_DAMAGED        = BlockDustyStoneUnstable(buildDustyStoneDamaged) named "dusty_stone_damaged"
	@JvmField val DUSTY_STONE_BRICKS         = BlockDustyStoneBricks(buildDustyStoneBricks) named "dusty_stone_bricks"
	@JvmField val DUSTY_STONE_CRACKED_BRICKS = BlockDustyStoneBricks.Cracked(buildDustyStoneBricks) named "dusty_stone_cracked_bricks"
	@JvmField val DUSTY_STONE_DECORATION     = BlockDustyStoneBricks(buildDustyStoneBricks) named "dusty_stone_decoration"
	@JvmField val DUSTY_STONE_BRICK_STAIRS   = BlockStairsCustom(DUSTY_STONE_BRICKS) named "dusty_stone_brick_stairs"
	@JvmField val DUSTY_STONE_BRICK_SLAB     = BlockSlabCustom(DUSTY_STONE_BRICKS) named "dusty_stone_brick_slab"
	
	// Blocks: Building (Obsidian)
	
	@JvmField val OBSIDIAN_STAIRS       = BlockStairsCustom(Blocks.OBSIDIAN) named "obsidian_stairs"
	@JvmField val OBSIDIAN_FALLING      = BlockFallingObsidian(buildObsidian) named "obsidian_falling"
	@JvmField val OBSIDIAN_SMOOTH       = BlockObsidianCube(buildObsidianVariation) named "obsidian_smooth"
	@JvmField val OBSIDIAN_CHISELED     = BlockObsidianCube(buildObsidianVariation) named "obsidian_chiseled"
	@JvmField val OBSIDIAN_PILLAR       = BlockObsidianPillar(buildObsidianVariation) named "obsidian_pillar"
	@JvmField val OBSIDIAN_SMOOTH_LIT   = BlockObsidianCube.Lit(buildObsidianVariationLit, OBSIDIAN_SMOOTH) named "obsidian_smooth_lit"
	@JvmField val OBSIDIAN_CHISELED_LIT = BlockObsidianCube.Lit(buildObsidianVariationLit, OBSIDIAN_CHISELED) named "obsidian_chiseled_lit"
	@JvmField val OBSIDIAN_PILLAR_LIT   = BlockObsidianPillar.Lit(buildObsidianVariationLit, OBSIDIAN_PILLAR) named "obsidian_pillar_lit"
	@JvmField val OBSIDIAN_TOWER_TOP    = BlockObsidianCube.TowerTop(buildObsidianTowerTop, OBSIDIAN_CHISELED) named "obsidian_tower_top"
	
	// Blocks: Building (End Stone)
	
	@JvmField val END_STONE_INFESTED  = BlockEndStoneCustom(buildEndStone, MaterialColor.RED) named "end_stone_infested"
	@JvmField val END_STONE_BURNED    = BlockEndStoneCustom(buildEndStone, MaterialColor.ADOBE /* RENAME ORANGE */) named "end_stone_burned"
	@JvmField val END_STONE_ENCHANTED = BlockEndStoneCustom(buildEndStone, MaterialColor.PURPLE) named "end_stone_enchanted"
	
	// Blocks: Building (Dark Loam)
	
	@JvmField val DARK_LOAM      = HeeBlock(buildDarkLoam) named "dark_loam"
	@JvmField val DARK_LOAM_SLAB = BlockSlabCustom(DARK_LOAM) named "dark_loam_slab"
	
	// Blocks: Building (Grave Dirt)
	
	@JvmField val GRAVE_DIRT_PLAIN      = BlockGraveDirt.Plain(buildGraveDirt) named "grave_dirt"
	@JvmField val GRAVE_DIRT_LOOT       = BlockGraveDirt.Loot(buildGraveDirt) named "grave_dirt_loot"
	@JvmField val GRAVE_DIRT_SPIDERLING = BlockGraveDirt.Spiderling(buildGraveDirt) named "grave_dirt_spiderling"
	
	// Blocks: Building (Wood)
	
	@JvmField val WHITEBARK_LOG    = BlockWhitebarkLog(buildWhitebark) named "whitebark_log"
	@JvmField val WHITEBARK        = BlockWhitebark(buildWhitebark) named "whitebark"
	@JvmField val WHITEBARK_PLANKS = BlockWhitebarkPlanks(buildWhitebarkPlanks) named "whitebark_planks"
	@JvmField val WHITEBARK_STAIRS = BlockFlammableStairs(WHITEBARK_PLANKS) named "whitebark_stairs"
	@JvmField val WHITEBARK_SLAB   = BlockFlammableSlab(WHITEBARK_PLANKS) named "whitebark_slab"
	
	// Blocks: Building (Miner's Burial)
	
	@JvmField val MINERS_BURIAL_BLOCK_PLAIN    = BlockMinersBurialCube(buildMinersBurial) named "miners_burial_block_plain"
	@JvmField val MINERS_BURIAL_BLOCK_CHISELED = BlockMinersBurialCube(buildMinersBurial) named "miners_burial_block_chiseled"
	@JvmField val MINERS_BURIAL_BLOCK_PILLAR   = BlockMinersBurialPillar(buildMinersBurial) named "miners_burial_block_pillar"
	@JvmField val MINERS_BURIAL_BLOCK_JAIL     = BlockMinersBurialCube(buildMinersBurialIndestructible) named "miners_burial_block_jail"
	@JvmField val MINERS_BURIAL_ALTAR          = BlockMinersBurialAltar(buildMinersBurialIndestructible) named "miners_burial_altar"
	
	// Blocks: Fluids
	
	@JvmField val ENDER_GOO          = BlockEnderGoo() named "ender_goo"
	@JvmField val PURIFIED_ENDER_GOO = BlockEnderGooPurified() named "purified_ender_goo"
	
	@JvmField val CAULDRON_ENDER_GOO          = BlockCauldronWithGoo(buildCauldron, ENDER_GOO) named "cauldron_ender_goo"
	@JvmField val CAULDRON_PURIFIED_ENDER_GOO = BlockCauldronWithGoo(buildCauldron, PURIFIED_ENDER_GOO) named "cauldron_purified_ender_goo"
	@JvmField val CAULDRON_DRAGONS_BREATH     = BlockCauldronWithDragonsBreath(buildCauldron) named "cauldron_dragons_breath"
	
	// Blocks: Interactive (Storage)
	
	@JvmField val JAR_O_DUST = BlockJarODust(buildJarODust) named "jar_o_dust"
	@JvmField val DARK_CHEST = BlockDarkChest(buildGloomrock) named "dark_chest"
	@JvmField val LOOT_CHEST = BlockLootChest(buildLootChest) named "loot_chest"
	
	// Blocks: Interactive (Puzzle)
	
	@JvmField val PUZZLE_WALL       = BlockPuzzleWall(buildPuzzleWall) named "puzzle_block_wall"
	@JvmField val PUZZLE_PLAIN      = BlockPuzzleLogic.Plain(buildPuzzleLogic) named "puzzle_block_plain"
	@JvmField val PUZZLE_BURST_3    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 1) named "puzzle_block_burst_3"
	@JvmField val PUZZLE_BURST_5    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 2) named "puzzle_block_burst_5"
	@JvmField val PUZZLE_REDIRECT_1 = BlockPuzzleLogic.RedirectSome.R1(buildPuzzleLogic) named "puzzle_block_redirect_1"
	@JvmField val PUZZLE_REDIRECT_2 = BlockPuzzleLogic.RedirectSome.R2(buildPuzzleLogic) named "puzzle_block_redirect_2"
	@JvmField val PUZZLE_REDIRECT_4 = BlockPuzzleLogic.RedirectAll(buildPuzzleLogic) named "puzzle_block_redirect_4"
	@JvmField val PUZZLE_TELEPORT   = BlockPuzzleLogic.Teleport(buildPuzzleLogic) named "puzzle_block_teleport"
	
	// Blocks: Interactive (Gates)
	
	@JvmField val EXPERIENCE_GATE            = BlockExperienceGateOutline(buildExperienceGate) named "experience_gate"
	@JvmField val EXPERIENCE_GATE_CONTROLLER = BlockExperienceGateController(buildExperienceGate) named "experience_gate_controller"
	
	// Blocks: Interactive (Uncategorized)
	
	@JvmField val INFUSED_TNT            = BlockInfusedTNT() named "infused_tnt"
	@JvmField val IGNEOUS_PLATE          = BlockIgneousPlate(buildIgneousPlate) named "igneous_plate"
	@JvmField val ENHANCED_BREWING_STAND = BlockBrewingStandCustom(buildBrewingStand) named "enhanced_brewing_stand"
	
	// Blocks: Ores
	
	@JvmField val END_POWDER_ORE   = BlockEndPowderOre(buildEndPowderOre) named "end_powder_ore"
	@JvmField val ENDIUM_ORE       = BlockEndium.Ore(buildEndiumOre) named "endium_ore"
	@JvmField val STARDUST_ORE     = BlockStardustOre(buildStardustOre) named "stardust_ore"
	@JvmField val IGNEOUS_ROCK_ORE = BlockIgneousRockOre(buildIgneousRockOre) named "igneous_rock_ore"
	
	// Blocks: Decorative (Trees)
	
	@JvmField val WHITEBARK_SAPLING_AUTUMN_RED         = BlockWhitebarkSapling.Autumn(buildWhitebarkSapling, AutumnTreeGenerator.Red) named "autumn_sapling_red"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_BROWN       = BlockWhitebarkSapling.Autumn(buildWhitebarkSapling, AutumnTreeGenerator.Brown) named "autumn_sapling_brown"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_ORANGE      = BlockWhitebarkSapling.Autumn(buildWhitebarkSapling, AutumnTreeGenerator.Orange) named "autumn_sapling_orange"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN = BlockWhitebarkSapling.Autumn(buildWhitebarkSapling, AutumnTreeGenerator.YellowGreen) named "autumn_sapling_yellowgreen"
	
	@JvmField val WHITEBARK_LEAVES_AUTUMN_RED         = BlockWhitebarkLeaves.Autumn(buildWhitebarkLeaves, MaterialColor.RED) named "autumn_leaves_red"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_BROWN       = BlockWhitebarkLeaves.Autumn(buildWhitebarkLeaves, MaterialColor.BROWN_TERRACOTTA) named "autumn_leaves_brown"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_ORANGE      = BlockWhitebarkLeaves.Autumn(buildWhitebarkLeaves, MaterialColor.ADOBE /* RENAME ORANGE */) named "autumn_leaves_orange"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN = BlockWhitebarkLeaves.Autumn(buildWhitebarkLeaves, MaterialColor.YELLOW) named "autumn_leaves_yellowgreen"
	
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_RED         = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_RED) named "potted_autumn_sapling_red"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_BROWN       = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_BROWN) named "potted_autumn_sapling_brown"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_ORANGE      = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_ORANGE) named "potted_autumn_sapling_orange"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN) named "potted_autumn_sapling_yellowgreen"
	
	// Blocks: Decorative (Plants)
	
	@JvmField val DEATH_FLOWER_DECAYING = BlockDeathFlowerDecaying(buildPlant) named "death_flower"
	@JvmField val DEATH_FLOWER_HEALED   = BlockDeathFlower(buildPlant) named "death_flower_healed"
	@JvmField val DEATH_FLOWER_WITHERED = BlockDeathFlower(buildPlant) named "death_flower_withered"
	
	@JvmField val POTTED_DEATH_FLOWER_DECAYING = BlockFlowerPotDeathFlowerDecaying(buildFlowerPot, DEATH_FLOWER_DECAYING) named "potted_death_flower"
	@JvmField val POTTED_DEATH_FLOWER_HEALED   = BlockFlowerPotDeathFlower(buildFlowerPot, DEATH_FLOWER_HEALED) named "potted_death_flower_healed"
	@JvmField val POTTED_DEATH_FLOWER_WITHERED = BlockFlowerPotDeathFlower(buildFlowerPot, DEATH_FLOWER_WITHERED) named "potted_death_flower_withered"
	
	// Blocks: Decorative (Uncategorized)
	
	@JvmField val ANCIENT_COBWEB     = BlockAncientCobweb(buildAncientCobweb) named "ancient_cobweb"
	@JvmField val DRY_VINES          = BlockDryVines(buildDryVines) named "dry_vines"
	@JvmField val ENDERMAN_HEAD      = BlockSkullCustom(CustomSkull.Enderman, buildEndermanHead) named "enderman_head"
	@JvmField val ENDERMAN_WALL_HEAD = BlockSkullCustom.Wall(CustomSkull.Enderman, buildEndermanHead) named "enderman_wall_head"
	
	// Blocks: Spawners
	
	@JvmField val SPAWNER_OBSIDIAN_TOWERS = BlockSpawnerObsidianTowers(buildSpawnerObsidianTowers) named "spawner_obsidian_towers"
	
	// Blocks: Portals
	
	@JvmField val END_PORTAL_INNER    = BlockEndPortalInner(buildPortalInner) named "end_portal_inner"
	@JvmField val END_PORTAL_FRAME    = BlockPortalFrame(buildPortalFrame) named "end_portal_frame"
	@JvmField val END_PORTAL_ACCEPTOR = BlockEndPortalAcceptor(buildPortalFrame) named "end_portal_acceptor"
	
	@JvmField val VOID_PORTAL_INNER   = BlockVoidPortalInner(buildPortalInner) named "void_portal_inner"
	@JvmField val VOID_PORTAL_FRAME   = BlockPortalFrame(buildPortalFrame) named "void_portal_frame"
	@JvmField val VOID_PORTAL_STORAGE = BlockVoidPortalStorage(buildPortalFrame) named "void_portal_storage"
	
	@JvmField val VOID_PORTAL_FRAME_CRAFTED   = BlockVoidPortalCrafted(buildPortalFrameCrafted) named "void_portal_frame_crafted"
	@JvmField val VOID_PORTAL_STORAGE_CRAFTED = BlockVoidPortalStorageCrafted(buildPortalFrameCrafted) named "void_portal_storage_crafted"
	
	// Blocks: Energy
	
	@JvmField val ENERGY_CLUSTER   = BlockEnergyCluster(buildEnergyCluster) named "energy_cluster"
	@JvmField val CORRUPTED_ENERGY = BlockCorruptedEnergy(buildCorruptedEnergy) named "corrupted_energy"
	
	// Blocks: Tables
	
	@JvmField val TABLE_PEDESTAL            = BlockTablePedestal(buildTablePedestal) named "table_pedestal"
	@JvmField val TABLE_BASE_TIER_1         = BlockTableBase(buildTable, tier = 1, firstTier = 1) named "table_base_tier_1"
	@JvmField val TABLE_BASE_TIER_2         = BlockTableBase(buildTable, tier = 2, firstTier = 1) named "table_base_tier_2"
	@JvmField val TABLE_BASE_TIER_3         = BlockTableBase(buildTable, tier = 3, firstTier = 1) named "table_base_tier_3"
	@JvmField val ACCUMULATION_TABLE_TIER_1 = BlockTableTile<TileEntityAccumulationTable>(buildTable, "accumulation_table", tier = 1, firstTier = 1) named "accumulation_table_tier_1"
	@JvmField val ACCUMULATION_TABLE_TIER_2 = BlockTableTile<TileEntityAccumulationTable>(buildTable, "accumulation_table", tier = 2, firstTier = 1) named "accumulation_table_tier_2"
	@JvmField val ACCUMULATION_TABLE_TIER_3 = BlockTableTile<TileEntityAccumulationTable>(buildTable, "accumulation_table", tier = 3, firstTier = 1) named "accumulation_table_tier_3"
	@JvmField val EXPERIENCE_TABLE_TIER_1   = BlockTableTile<TileEntityExperienceTable>(buildTable, "experience_table", tier = 1, firstTier = 1) named "experience_table_tier_1"
	@JvmField val EXPERIENCE_TABLE_TIER_2   = BlockTableTile<TileEntityExperienceTable>(buildTable, "experience_table", tier = 2, firstTier = 1) named "experience_table_tier_2"
	@JvmField val EXPERIENCE_TABLE_TIER_3   = BlockTableTile<TileEntityExperienceTable>(buildTable, "experience_table", tier = 3, firstTier = 1) named "experience_table_tier_3"
	@JvmField val INFUSION_TABLE_TIER_1     = BlockTableTile<TileEntityInfusionTable>(buildTable, "infusion_table", tier = 1, firstTier = 1) named "infusion_table_tier_1"
	@JvmField val INFUSION_TABLE_TIER_2     = BlockTableTile<TileEntityInfusionTable>(buildTable, "infusion_table", tier = 2, firstTier = 1) named "infusion_table_tier_2"
	@JvmField val INFUSION_TABLE_TIER_3     = BlockTableTile<TileEntityInfusionTable>(buildTable, "infusion_table", tier = 3, firstTier = 1) named "infusion_table_tier_3"
	
	// Blocks: Utilities
	
	@JvmField val ETERNAL_FIRE = BlockEternalFire(buildEternalFire) named "eternal_fire"
	@JvmField val SCAFFOLDING  = BlockScaffolding.create(buildScaffolding) named "scaffolding"
	
	// Registry
	
	private val itemBlockDefaultProps: Item.Properties.() -> Item.Properties = { group(ModCreativeTabs.main) }
	private val itemBlockPropsHidden: Item.Properties.() -> Item.Properties = { this }
	
	private val basicItemBlock = { block: Block -> BlockItem(block, itemBlockDefaultProps(Item.Properties())) }
	private val hiddenItemBlock = { block: Block -> BlockItem(block, itemBlockPropsHidden(Item.Properties())) }
	
	private fun fuelItemBlock(burnTicks: Int): (Block) -> BlockItem = {
		HeeBlockItemBuilder(it) { components.furnaceBurnTime = burnTicks }.build { group(ModCreativeTabs.main) }
	}
	
	@SubscribeEvent
	fun onRegisterFluids(e: RegistryEvent.Register<Fluid>) {
		with(e.registry) {
			register(FluidEnderGoo.still)
			register(FluidEnderGoo.flowing)
			register(FluidEnderGooPurified.still)
			register(FluidEnderGooPurified.flowing)
		}
	}
	
	@SubscribeEvent
	fun onRegisterBlocks(e: RegistryEvent.Register<Block>) {
		with(e.registry) {
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
			register(GLOOMROCK_BRICK_SLAB with basicItemBlock)
			register(GLOOMROCK_SMOOTH with basicItemBlock)
			register(GLOOMROCK_SMOOTH_STAIRS with basicItemBlock)
			register(GLOOMROCK_SMOOTH_SLAB with basicItemBlock)
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
			register(DUSTY_STONE_DECORATION with basicItemBlock)
			register(DUSTY_STONE_BRICK_STAIRS with basicItemBlock)
			register(DUSTY_STONE_BRICK_SLAB with basicItemBlock)
			
			register(OBSIDIAN_STAIRS with basicItemBlock)
			register(OBSIDIAN_FALLING with basicItemBlock)
			register(OBSIDIAN_SMOOTH with basicItemBlock)
			register(OBSIDIAN_CHISELED with basicItemBlock)
			register(OBSIDIAN_PILLAR with basicItemBlock)
			register(OBSIDIAN_SMOOTH_LIT with basicItemBlock)
			register(OBSIDIAN_CHISELED_LIT with basicItemBlock)
			register(OBSIDIAN_PILLAR_LIT with basicItemBlock)
			register(OBSIDIAN_TOWER_TOP with hiddenItemBlock)
			
			register(END_STONE_INFESTED with basicItemBlock)
			register(END_STONE_BURNED with basicItemBlock)
			register(END_STONE_ENCHANTED with basicItemBlock)
			
			register(DARK_LOAM with basicItemBlock)
			register(DARK_LOAM_SLAB with basicItemBlock)
			
			register(GRAVE_DIRT_PLAIN with basicItemBlock)
			register(GRAVE_DIRT_LOOT with basicItemBlock)
			register(GRAVE_DIRT_SPIDERLING with basicItemBlock)
			
			register(WHITEBARK_LOG with basicItemBlock)
			register(WHITEBARK with basicItemBlock)
			register(WHITEBARK_PLANKS with basicItemBlock)
			register(WHITEBARK_STAIRS with basicItemBlock)
			register(WHITEBARK_SLAB with fuelItemBlock(burnTicks = 150))
			
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
			
			register(JAR_O_DUST with { BlockItem(it, itemBlockDefaultProps(Item.Properties()).maxStackSize(1).setISTER { ModRendering.RENDER_ITEM_JAR_O_DUST }) })
			register(DARK_CHEST with { BlockItem(it, itemBlockDefaultProps(Item.Properties()).setISTER { ModRendering.RENDER_ITEM_DARK_CHEST }) })
			register(LOOT_CHEST with { BlockItem(it, itemBlockDefaultProps(Item.Properties()).setISTER { ModRendering.RENDER_ITEM_LOOT_CHEST }) })
			
			register(PUZZLE_WALL with basicItemBlock)
			register(PUZZLE_PLAIN with basicItemBlock)
			register(PUZZLE_BURST_3 with basicItemBlock)
			register(PUZZLE_BURST_5 with basicItemBlock)
			register(PUZZLE_REDIRECT_1 with basicItemBlock)
			register(PUZZLE_REDIRECT_2 with basicItemBlock)
			register(PUZZLE_REDIRECT_4 with basicItemBlock)
			register(PUZZLE_TELEPORT with basicItemBlock)
			
			register(INFUSED_TNT with { ItemInfusedTNT(it).build() })
			register(IGNEOUS_PLATE with basicItemBlock)
			register(ENHANCED_BREWING_STAND with basicItemBlock)
			register(EXPERIENCE_GATE with basicItemBlock)
			register(EXPERIENCE_GATE_CONTROLLER)
			
			register(END_POWDER_ORE with basicItemBlock)
			register(ENDIUM_ORE with basicItemBlock)
			register(STARDUST_ORE with basicItemBlock)
			register(IGNEOUS_ROCK_ORE with basicItemBlock)
			
			register(WHITEBARK_LEAVES_AUTUMN_RED with basicItemBlock)
			register(WHITEBARK_LEAVES_AUTUMN_BROWN with basicItemBlock)
			register(WHITEBARK_LEAVES_AUTUMN_ORANGE with basicItemBlock)
			register(WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN with basicItemBlock)
			register(WHITEBARK_SAPLING_AUTUMN_BROWN with fuelItemBlock(burnTicks = 100))
			register(WHITEBARK_SAPLING_AUTUMN_RED with fuelItemBlock(burnTicks = 100))
			register(WHITEBARK_SAPLING_AUTUMN_ORANGE with fuelItemBlock(burnTicks = 100))
			register(WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN with fuelItemBlock(burnTicks = 100))
			register(POTTED_WHITEBARK_SAPLING_AUTUMN_RED)
			register(POTTED_WHITEBARK_SAPLING_AUTUMN_BROWN)
			register(POTTED_WHITEBARK_SAPLING_AUTUMN_ORANGE)
			register(POTTED_WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN)
			
			register(DEATH_FLOWER_DECAYING with { ItemDeathFlower(it).build(itemBlockDefaultProps) })
			register(DEATH_FLOWER_HEALED with basicItemBlock)
			register(DEATH_FLOWER_WITHERED with basicItemBlock)
			register(POTTED_DEATH_FLOWER_DECAYING)
			register(POTTED_DEATH_FLOWER_HEALED)
			register(POTTED_DEATH_FLOWER_WITHERED)
			
			register(ANCIENT_COBWEB with { ItemAncientCobweb(it).build(itemBlockDefaultProps) })
			register(DRY_VINES with basicItemBlock)
			register(ENDERMAN_HEAD)
			
			register(SPAWNER_OBSIDIAN_TOWERS)
			
			register(END_PORTAL_INNER)
			register(END_PORTAL_FRAME with basicItemBlock)
			register(END_PORTAL_ACCEPTOR with basicItemBlock)
			register(VOID_PORTAL_INNER)
			register(VOID_PORTAL_FRAME with basicItemBlock)
			register(VOID_PORTAL_STORAGE with basicItemBlock)
			register(VOID_PORTAL_FRAME_CRAFTED with hiddenItemBlock)
			register(VOID_PORTAL_STORAGE_CRAFTED with hiddenItemBlock)
			
			register(ENERGY_CLUSTER with basicItemBlock)
			register(CORRUPTED_ENERGY)
			
			register(TABLE_PEDESTAL with basicItemBlock)
			register(TABLE_BASE_TIER_1 with basicItemBlock)
			register(TABLE_BASE_TIER_2 with basicItemBlock)
			register(TABLE_BASE_TIER_3 with basicItemBlock)
			register(ACCUMULATION_TABLE_TIER_1 with basicItemBlock)
			register(ACCUMULATION_TABLE_TIER_2 with basicItemBlock)
			register(ACCUMULATION_TABLE_TIER_3 with basicItemBlock)
			register(EXPERIENCE_TABLE_TIER_1 with basicItemBlock)
			register(EXPERIENCE_TABLE_TIER_2 with basicItemBlock)
			register(EXPERIENCE_TABLE_TIER_3 with basicItemBlock)
			register(INFUSION_TABLE_TIER_1 with basicItemBlock)
			register(INFUSION_TABLE_TIER_2 with basicItemBlock)
			register(INFUSION_TABLE_TIER_3 with basicItemBlock)
			
			register(ETERNAL_FIRE)
			register(SCAFFOLDING with basicItemBlock)
		}
		
		// vanilla modifications
		
		with(e.registry) {
			register(BlockEndPortalOverride(buildEndPortalOverride).override(Blocks.END_PORTAL) { null })
			register(BlockBrewingStandCustom.Override(buildBrewingStand).override(Blocks.BREWING_STAND) { BlockItem(it, Item.Properties().group(ItemGroup.BREWING)) })
			register(BlockDragonEggOverride(buildDragonEgg).override(Blocks.DRAGON_EGG) { ItemDragonEgg(it).build(itemBlockDefaultProps) })
			
			for (block in BlockShulkerBoxOverride.ALL_BLOCKS) {
				register(BlockShulkerBoxOverride(AbstractBlock.Properties.from(block), block.color).override(block) {
					ItemShulkerBoxOverride(it).build { group(ItemGroup.DECORATIONS) }
				})
			}
		}
	}
	
	@SubscribeEvent
	fun onRegisterItemBlocks(e: RegistryEvent.Register<Item>) {
		temporaryItemBlocks.forEach(e.registry::register)
		temporaryItemBlocks.clear()
		
		// vanilla modifications
		
		Blocks.END_PORTAL_FRAME.asItem().group = null
	}
	
	// Utilities
	
	private val temporaryItemBlocks = mutableListOf<BlockItem>()
	private val overrideBlocks = mutableListOf<Block>()
	
	private inline fun Block.override(vanillaBlock: Block, itemBlockConstructor: ((Block) -> BlockItem?)) = apply {
		this.useVanillaName(vanillaBlock)
		overrideBlocks.add(this)
		itemBlockConstructor(this)?.let { with(it); ModItems.registerOverride(it) }
	}
	
	private infix fun Block.with(itemBlock: BlockItem) = apply {
		if (Resource.isVanilla(this.registryName!!)) {
			itemBlock.useVanillaName(this)
		}
		else {
			itemBlock.registryName = this.registryName
		}
		
		temporaryItemBlocks.add(itemBlock)
		(itemBlock.group as? OrderedCreativeTab)?.registerOrder(itemBlock)
	}
	
	private infix fun <T : Block> T.with(itemBlockConstructor: (T) -> BlockItem): Block {
		return with(itemBlockConstructor(this))
	}
}
