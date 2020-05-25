package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.game.block.BlockBrewingStandCustom
import chylex.hee.game.block.BlockCauldronWithDragonsBreath
import chylex.hee.game.block.BlockCauldronWithGoo
import chylex.hee.game.block.BlockCorruptedEnergy
import chylex.hee.game.block.BlockDarkChest
import chylex.hee.game.block.BlockDeathFlowerDecaying
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockDustyStoneBricks
import chylex.hee.game.block.BlockDustyStoneUnstable
import chylex.hee.game.block.BlockEndPlant
import chylex.hee.game.block.BlockEndPortalAcceptor
import chylex.hee.game.block.BlockEndPortalInner
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.block.BlockEndPowderOre
import chylex.hee.game.block.BlockEnderGoo
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.block.BlockEndersol
import chylex.hee.game.block.BlockEndium
import chylex.hee.game.block.BlockEnergyCluster
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
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.BlockSimple
import chylex.hee.game.block.BlockSimpleShaped
import chylex.hee.game.block.BlockSimpleWithMapColor
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
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.block.BlockWhitebarkLog
import chylex.hee.game.block.BlockWhitebarkSapling
import chylex.hee.game.block.entity.TileEntityAccumulationTable
import chylex.hee.game.block.entity.TileEntityExperienceTable
import chylex.hee.game.block.entity.TileEntityInfusionTable
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.info.BlockBuilders.buildAncientCobweb
import chylex.hee.game.block.info.BlockBuilders.buildBrewingStand
import chylex.hee.game.block.info.BlockBuilders.buildCauldron
import chylex.hee.game.block.info.BlockBuilders.buildCorruptedEnergy
import chylex.hee.game.block.info.BlockBuilders.buildDarkLoam
import chylex.hee.game.block.info.BlockBuilders.buildDragonEgg
import chylex.hee.game.block.info.BlockBuilders.buildDryVines
import chylex.hee.game.block.info.BlockBuilders.buildDustyStone
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneBricks
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneCracked
import chylex.hee.game.block.info.BlockBuilders.buildDustyStoneDamaged
import chylex.hee.game.block.info.BlockBuilders.buildEndPortalOverride
import chylex.hee.game.block.info.BlockBuilders.buildEndPowderOre
import chylex.hee.game.block.info.BlockBuilders.buildEndStone
import chylex.hee.game.block.info.BlockBuilders.buildEnderSol
import chylex.hee.game.block.info.BlockBuilders.buildEndermanHead
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
import chylex.hee.game.block.info.BlockBuilders.buildMinersBurialIndestructible
import chylex.hee.game.block.info.BlockBuilders.buildObsidian
import chylex.hee.game.block.info.BlockBuilders.buildObsidianVariation
import chylex.hee.game.block.info.BlockBuilders.buildObsidianVariationLit
import chylex.hee.game.block.info.BlockBuilders.buildPlant
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
import chylex.hee.game.block.info.BlockBuilders.buildWhitebarkLeaves
import chylex.hee.game.block.info.BlockBuilders.buildWhitebarkPlanks
import chylex.hee.game.block.info.BlockBuilders.buildWhitebarkSapling
import chylex.hee.game.block.util.CustomSkulls
import chylex.hee.game.item.ItemAncientCobweb
import chylex.hee.game.item.ItemBlockFuel
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.game.item.ItemDragonEgg
import chylex.hee.game.item.ItemInfusedTNT
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.world.feature.basic.trees.types.AutumnTreeGenerator
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.BlockFire
import chylex.hee.system.migration.vanilla.BlockWall
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.named
import chylex.hee.system.util.useVanillaName
import net.minecraft.block.Block
import net.minecraft.block.material.MaterialColor
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import java.util.concurrent.Callable

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModBlocks{
	
	// Blocks: Building (Uncategorized)
	
	@JvmField val ETHEREAL_LANTERN = BlockSimple(buildEtherealLantern) named "ethereal_lantern"
	@JvmField val STONE_BRICK_WALL = BlockWall(Block.Properties.from(Blocks.STONE_BRICKS)) named "stone_brick_wall"
	@JvmField val INFUSED_GLASS    = BlockInfusedGlass(buildInfusedGlass) named "infused_glass"
	@JvmField val VANTABLOCK       = BlockSimple(buildVantablock) named "vantablock"
	@JvmField val ENDIUM_BLOCK     = BlockEndium(buildEndiumBlock) named "endium_block"
	@JvmField val ENDERSOL         = BlockEndersol(buildEnderSol) named "endersol"
	@JvmField val HUMUS            = BlockHumus(buildHumus) named "humus"
	
	// Blocks: Building (Gloomrock)
	
	@JvmField val GLOOMROCK                = BlockGloomrock(buildGloomrock) named "gloomrock"
	@JvmField val GLOOMROCK_BRICKS         = BlockGloomrock(buildGloomrockBricks) named "gloomrock_bricks"
	@JvmField val GLOOMROCK_BRICK_STAIRS   = BlockStairsCustom(GLOOMROCK_BRICKS) named "gloomrock_brick_stairs"
	@JvmField val GLOOMROCK_BRICK_SLAB     = BlockSlabCustom(buildGloomrockBricks) named "gloomrock_brick_slab"
	@JvmField val GLOOMROCK_SMOOTH         = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth"
	@JvmField val GLOOMROCK_SMOOTH_STAIRS  = BlockStairsCustom(GLOOMROCK_SMOOTH) named "gloomrock_smooth_stairs"
	@JvmField val GLOOMROCK_SMOOTH_SLAB    = BlockSlabCustom(buildGloomrockSmooth) named "gloomrock_smooth_slab"
	@JvmField val GLOOMROCK_SMOOTH_RED     = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_red"
	@JvmField val GLOOMROCK_SMOOTH_ORANGE  = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_orange"
	@JvmField val GLOOMROCK_SMOOTH_YELLOW  = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_yellow"
	@JvmField val GLOOMROCK_SMOOTH_GREEN   = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_green"
	@JvmField val GLOOMROCK_SMOOTH_CYAN    = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_cyan"
	@JvmField val GLOOMROCK_SMOOTH_BLUE    = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_blue"
	@JvmField val GLOOMROCK_SMOOTH_PURPLE  = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_purple"
	@JvmField val GLOOMROCK_SMOOTH_MAGENTA = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_magenta"
	@JvmField val GLOOMROCK_SMOOTH_WHITE   = BlockGloomrock(buildGloomrockSmooth) named "gloomrock_smooth_white"
	@JvmField val GLOOMTORCH               = BlockGloomtorch(buildGloomtorch) named "gloomtorch"
	
	// Blocks: Building (Dusty Stone)
	
	@JvmField val DUSTY_STONE                = BlockDustyStoneUnstable(buildDustyStone) named "dusty_stone"
	@JvmField val DUSTY_STONE_CRACKED        = BlockDustyStoneUnstable(buildDustyStoneCracked) named "dusty_stone_cracked"
	@JvmField val DUSTY_STONE_DAMAGED        = BlockDustyStoneUnstable(buildDustyStoneDamaged) named "dusty_stone_damaged"
	@JvmField val DUSTY_STONE_BRICKS         = BlockDustyStoneBricks(buildDustyStoneBricks) named "dusty_stone_bricks"
	@JvmField val DUSTY_STONE_CRACKED_BRICKS = BlockDustyStoneBricks(buildDustyStoneBricks) named "dusty_stone_cracked_bricks"
	@JvmField val DUSTY_STONE_DECORATION     = BlockDustyStoneBricks(buildDustyStoneBricks) named "dusty_stone_decoration"
	@JvmField val DUSTY_STONE_BRICK_STAIRS   = BlockStairsCustom(DUSTY_STONE_BRICKS) named "dusty_stone_brick_stairs"
	@JvmField val DUSTY_STONE_BRICK_SLAB     = BlockSlabCustom(buildDustyStoneBricks) named "dusty_stone_brick_slab"
	
	// Blocks: Building (Obsidian)
	
	@JvmField val OBSIDIAN_STAIRS       = BlockStairsCustom(Blocks.OBSIDIAN) named "obsidian_stairs"
	@JvmField val OBSIDIAN_FALLING      = BlockFallingObsidian(buildObsidian) named "obsidian_falling"
	@JvmField val OBSIDIAN_SMOOTH       = BlockSimple(buildObsidianVariation) named "obsidian_smooth"
	@JvmField val OBSIDIAN_CHISELED     = BlockSimple(buildObsidianVariation) named "obsidian_chiseled"
	@JvmField val OBSIDIAN_PILLAR       = BlockPillarCustom(buildObsidianVariation) named "obsidian_pillar"
	@JvmField val OBSIDIAN_SMOOTH_LIT   = BlockSimple(buildObsidianVariationLit) named "obsidian_smooth_lit"
	@JvmField val OBSIDIAN_CHISELED_LIT = BlockSimple(buildObsidianVariationLit) named "obsidian_chiseled_lit"
	@JvmField val OBSIDIAN_PILLAR_LIT   = BlockPillarCustom(buildObsidianVariationLit) named "obsidian_pillar_lit"
	
	// Blocks: Building (End Stone)
	
	@JvmField val END_STONE_INFESTED  = BlockSimpleWithMapColor(buildEndStone, MaterialColor.RED) named "end_stone_infested"
	@JvmField val END_STONE_BURNED    = BlockSimpleWithMapColor(buildEndStone, MaterialColor.ADOBE /* RENAME ORANGE */) named "end_stone_burned"
	@JvmField val END_STONE_ENCHANTED = BlockSimpleWithMapColor(buildEndStone, MaterialColor.PURPLE) named "end_stone_enchanted"
	
	// Blocks: Building (Dark Loam)
	
	@JvmField val DARK_LOAM      = BlockSimple(buildDarkLoam) named "dark_loam"
	@JvmField val DARK_LOAM_SLAB = BlockSlabCustom(buildDarkLoam) named "dark_loam_slab"
	
	// Blocks: Building (Grave Dirt)
	
	@JvmField val GRAVE_DIRT_PLAIN      = BlockGraveDirt(buildGraveDirt) named "grave_dirt"
	@JvmField val GRAVE_DIRT_LOOT       = BlockGraveDirt(buildGraveDirt) named "grave_dirt_loot"
	@JvmField val GRAVE_DIRT_SPIDERLING = BlockGraveDirt.Spiderling(buildGraveDirt) named "grave_dirt_spiderling"
	
	// Blocks: Building (Wood)
	
	@JvmField val WHITEBARK_LOG    = BlockWhitebarkLog(buildWhitebark) named "whitebark_log"
	@JvmField val WHITEBARK        = BlockSimple(buildWhitebark) named "whitebark"
	@JvmField val WHITEBARK_PLANKS = BlockSimple(buildWhitebarkPlanks) named "whitebark_planks"
	@JvmField val WHITEBARK_STAIRS = BlockStairsCustom(WHITEBARK_PLANKS) named "whitebark_stairs"
	@JvmField val WHITEBARK_SLAB   = BlockSlabCustom(buildWhitebarkPlanks) named "whitebark_slab"
	
	// Blocks: Building (Miner's Burial)
	
	@JvmField val MINERS_BURIAL_BLOCK_PLAIN    = BlockSimple(buildMinersBurial) named "miners_burial_block_plain"
	@JvmField val MINERS_BURIAL_BLOCK_CHISELED = BlockSimple(buildMinersBurial) named "miners_burial_block_chiseled"
	@JvmField val MINERS_BURIAL_BLOCK_PILLAR   = BlockPillarCustom(buildMinersBurial) named "miners_burial_block_pillar"
	@JvmField val MINERS_BURIAL_BLOCK_JAIL     = BlockSimple(buildMinersBurialIndestructible) named "miners_burial_block_jail"
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
	
	@JvmField val PUZZLE_WALL       = BlockSimple(buildPuzzleWall) named "puzzle_block_wall"
	@JvmField val PUZZLE_PLAIN      = BlockPuzzleLogic.Plain(buildPuzzleLogic) named "puzzle_block_plain"
	@JvmField val PUZZLE_BURST_3    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 1) named "puzzle_block_burst_3"
	@JvmField val PUZZLE_BURST_5    = BlockPuzzleLogic.Burst(buildPuzzleLogic, radius = 2) named "puzzle_block_burst_5"
	@JvmField val PUZZLE_REDIRECT_1 = BlockPuzzleLogic.Redirect(buildPuzzleLogic, arrayOf(NORTH)) named "puzzle_block_redirect_1"
	@JvmField val PUZZLE_REDIRECT_2 = BlockPuzzleLogic.Redirect(buildPuzzleLogic, arrayOf(NORTH, SOUTH)) named "puzzle_block_redirect_2"
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
	@JvmField val ENDIUM_ORE       = BlockEndium(buildEndiumOre) named "endium_ore"
	@JvmField val STARDUST_ORE     = BlockStardustOre(buildStardustOre) named "stardust_ore"
	@JvmField val IGNEOUS_ROCK_ORE = BlockIgneousRockOre(buildIgneousRockOre) named "igneous_rock_ore"
	
	// Blocks: Decorative (Trees)
	
	@JvmField val WHITEBARK_SAPLING_AUTUMN_RED         = BlockWhitebarkSapling(buildWhitebarkSapling, AutumnTreeGenerator.Red) named "autumn_sapling_red"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_BROWN       = BlockWhitebarkSapling(buildWhitebarkSapling, AutumnTreeGenerator.Brown) named "autumn_sapling_brown"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_ORANGE      = BlockWhitebarkSapling(buildWhitebarkSapling, AutumnTreeGenerator.Orange) named "autumn_sapling_orange"
	@JvmField val WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN = BlockWhitebarkSapling(buildWhitebarkSapling, AutumnTreeGenerator.YellowGreen) named "autumn_sapling_yellowgreen"
	
	@JvmField val WHITEBARK_LEAVES_AUTUMN_RED         = BlockWhitebarkLeaves(buildWhitebarkLeaves, MaterialColor.RED) named "autumn_leaves_red"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_BROWN       = BlockWhitebarkLeaves(buildWhitebarkLeaves, MaterialColor.BROWN_TERRACOTTA) named "autumn_leaves_brown"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_ORANGE      = BlockWhitebarkLeaves(buildWhitebarkLeaves, MaterialColor.ADOBE /* RENAME ORANGE */) named "autumn_leaves_orange"
	@JvmField val WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN = BlockWhitebarkLeaves(buildWhitebarkLeaves, MaterialColor.YELLOW) named "autumn_leaves_yellowgreen"
	
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_RED         = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_RED) named "potted_autumn_sapling_red"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_BROWN       = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_BROWN) named "potted_autumn_sapling_brown"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_ORANGE      = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_ORANGE) named "potted_autumn_sapling_orange"
	@JvmField val POTTED_WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN = BlockFlowerPotCustom(buildFlowerPot, WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN) named "potted_autumn_sapling_yellowgreen"
	
	// Blocks: Decorative (Plants)
	
	@JvmField val DEATH_FLOWER_DECAYING = BlockDeathFlowerDecaying(buildPlant) named "death_flower"
	@JvmField val DEATH_FLOWER_HEALED   = BlockEndPlant(buildPlant) named "death_flower_healed"
	@JvmField val DEATH_FLOWER_WITHERED = BlockEndPlant(buildPlant) named "death_flower_withered"
	
	@JvmField val POTTED_DEATH_FLOWER_DECAYING = BlockFlowerPotDeathFlowerDecaying(buildFlowerPot, DEATH_FLOWER_DECAYING) named "potted_death_flower"
	@JvmField val POTTED_DEATH_FLOWER_HEALED   = BlockFlowerPotCustom(buildFlowerPot, DEATH_FLOWER_HEALED) named "potted_death_flower_healed"
	@JvmField val POTTED_DEATH_FLOWER_WITHERED = BlockFlowerPotCustom(buildFlowerPot, DEATH_FLOWER_WITHERED) named "potted_death_flower_withered"
	
	// Blocks: Decorative (Uncategorized)
	
	@JvmField val ANCIENT_COBWEB     = BlockAncientCobweb(buildAncientCobweb) named "ancient_cobweb"
	@JvmField val DRY_VINES          = BlockDryVines(buildDryVines) named "dry_vines"
	@JvmField val ENDERMAN_HEAD      = BlockSkullCustom(CustomSkulls.Enderman, buildEndermanHead) named "enderman_head"
	@JvmField val ENDERMAN_WALL_HEAD = BlockSkullCustom.Wall(CustomSkulls.Enderman, buildEndermanHead) named "enderman_wall_head"
	
	// Blocks: Spawners
	
	@JvmField val SPAWNER_OBSIDIAN_TOWERS = BlockSpawnerObsidianTowers(buildSpawnerObsidianTowers) named "spawner_obsidian_towers"
	
	// Blocks: Portals
	
	private val portalFrameAABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0)
	
	@JvmField val END_PORTAL_INNER    = BlockEndPortalInner(buildPortalInner) named "end_portal_inner"
	@JvmField val END_PORTAL_FRAME    = BlockSimpleShaped(buildPortalFrame, portalFrameAABB) named "end_portal_frame"
	@JvmField val END_PORTAL_ACCEPTOR = BlockEndPortalAcceptor(buildPortalFrame, portalFrameAABB) named "end_portal_acceptor"
	
	@JvmField val VOID_PORTAL_INNER   = BlockVoidPortalInner(buildPortalInner) named "void_portal_inner"
	@JvmField val VOID_PORTAL_FRAME   = BlockSimpleShaped(buildPortalFrame, portalFrameAABB) named "void_portal_frame"
	@JvmField val VOID_PORTAL_STORAGE = BlockVoidPortalStorage(buildPortalFrame, portalFrameAABB) named "void_portal_storage"
	
	@JvmField val VOID_PORTAL_FRAME_CRAFTED   = BlockVoidPortalCrafted(buildPortalFrameCrafted, portalFrameAABB) named "void_portal_frame_crafted"
	@JvmField val VOID_PORTAL_STORAGE_CRAFTED = BlockVoidPortalStorageCrafted(buildPortalFrameCrafted, portalFrameAABB) named "void_portal_storage_crafted"
	
	// Blocks: Energy
	
	@JvmField val ENERGY_CLUSTER   = BlockEnergyCluster(buildEnergyCluster) named "energy_cluster"
	@JvmField val CORRUPTED_ENERGY = BlockCorruptedEnergy(buildCorruptedEnergy) named "corrupted_energy"
	
	// Blocks: Tables
	
	@JvmField val TABLE_PEDESTAL            = BlockTablePedestal(buildTablePedestal) named "table_pedestal"
	@JvmField val TABLE_BASE_TIER_1         = BlockTableBase(buildTable, tier = 1, firstTier = 1) named "table_base_tier_1"
	@JvmField val TABLE_BASE_TIER_2         = BlockTableBase(buildTable, tier = 2, firstTier = 1) named "table_base_tier_2"
	@JvmField val TABLE_BASE_TIER_3         = BlockTableBase(buildTable, tier = 3, firstTier = 1) named "table_base_tier_3"
	@JvmField val ACCUMULATION_TABLE_TIER_1 = BlockTableTile(buildTable, "accumulation_table", TileEntityAccumulationTable::class.java, tier = 1, firstTier = 1) named "accumulation_table_tier_1"
	@JvmField val ACCUMULATION_TABLE_TIER_2 = BlockTableTile(buildTable, "accumulation_table", TileEntityAccumulationTable::class.java, tier = 2, firstTier = 1) named "accumulation_table_tier_2"
	@JvmField val ACCUMULATION_TABLE_TIER_3 = BlockTableTile(buildTable, "accumulation_table", TileEntityAccumulationTable::class.java, tier = 3, firstTier = 1) named "accumulation_table_tier_3"
	@JvmField val EXPERIENCE_TABLE_TIER_1   = BlockTableTile(buildTable, "experience_table", TileEntityExperienceTable::class.java, tier = 1, firstTier = 1) named "experience_table_tier_1"
	@JvmField val EXPERIENCE_TABLE_TIER_2   = BlockTableTile(buildTable, "experience_table", TileEntityExperienceTable::class.java, tier = 2, firstTier = 1) named "experience_table_tier_2"
	@JvmField val EXPERIENCE_TABLE_TIER_3   = BlockTableTile(buildTable, "experience_table", TileEntityExperienceTable::class.java, tier = 3, firstTier = 1) named "experience_table_tier_3"
	@JvmField val INFUSION_TABLE_TIER_1     = BlockTableTile(buildTable, "infusion_table", TileEntityInfusionTable::class.java, tier = 1, firstTier = 1) named "infusion_table_tier_1"
	@JvmField val INFUSION_TABLE_TIER_2     = BlockTableTile(buildTable, "infusion_table", TileEntityInfusionTable::class.java, tier = 2, firstTier = 1) named "infusion_table_tier_2"
	@JvmField val INFUSION_TABLE_TIER_3     = BlockTableTile(buildTable, "infusion_table", TileEntityInfusionTable::class.java, tier = 3, firstTier = 1) named "infusion_table_tier_3"
	
	// Blocks: Utilities
	
	@JvmField val ETERNAL_FIRE = BlockEternalFire(buildEternalFire) named "eternal_fire"
	@JvmField val SCAFFOLDING  = BlockScaffolding(buildScaffolding) named "scaffolding"
	
	// Registry
	
	private val itemBlockBaseProps
		get() = Item.Properties().group(ModCreativeTabs.main)
	
	private val itemBlockDefaultProps = itemBlockBaseProps
	private val itemBlockPropsHidden = Item.Properties()
	
	private val basicItemBlock = { block: Block -> ItemBlock(block, itemBlockDefaultProps) }
	private val hiddenItemBlock = { block: Block -> ItemBlock(block, itemBlockPropsHidden) }
	
	private fun fuelItemBlock(burnTicks: Int): (Block) -> ItemBlock{
		return { block -> ItemBlockFuel(block, itemBlockDefaultProps, burnTicks) }
	}
	
	@SubscribeEvent
	fun onRegisterFluids(e: RegistryEvent.Register<Fluid>){
		with(e.registry){
			register(FluidEnderGoo.still)
			register(FluidEnderGoo.flowing)
			register(FluidEnderGooPurified.still)
			register(FluidEnderGooPurified.flowing)
		}
	}
	
	@SubscribeEvent
	fun onRegisterBlocks(e: RegistryEvent.Register<Block>){
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
			
			register(JAR_O_DUST with { ItemBlock(it, itemBlockBaseProps.maxStackSize(1).setTEISR { Callable { ModRendering.RENDER_ITEM_JAR_O_DUST } }) })
			register(DARK_CHEST with { ItemBlock(it, itemBlockBaseProps.setTEISR { Callable { ModRendering.RENDER_ITEM_DARK_CHEST } }) })
			register(LOOT_CHEST with { ItemBlock(it, itemBlockBaseProps.setTEISR { Callable { ModRendering.RENDER_ITEM_LOOT_CHEST } }) })
			
			register(PUZZLE_WALL with basicItemBlock)
			register(PUZZLE_PLAIN with basicItemBlock)
			register(PUZZLE_BURST_3 with basicItemBlock)
			register(PUZZLE_BURST_5 with basicItemBlock)
			register(PUZZLE_REDIRECT_1 with basicItemBlock)
			register(PUZZLE_REDIRECT_2 with basicItemBlock)
			register(PUZZLE_REDIRECT_4 with basicItemBlock)
			register(PUZZLE_TELEPORT with basicItemBlock)
			
			register(INFUSED_TNT with ItemInfusedTNT(INFUSED_TNT, itemBlockPropsHidden))
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
			
			register(DEATH_FLOWER_DECAYING with { ItemDeathFlower(it, itemBlockDefaultProps) })
			register(DEATH_FLOWER_HEALED with basicItemBlock)
			register(DEATH_FLOWER_WITHERED with basicItemBlock)
			register(POTTED_DEATH_FLOWER_DECAYING)
			register(POTTED_DEATH_FLOWER_HEALED)
			register(POTTED_DEATH_FLOWER_WITHERED)
			
			register(ANCIENT_COBWEB with { ItemAncientCobweb(it, itemBlockDefaultProps) })
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
		
		with(e.registry){
			register(BlockEndPortalOverride(buildEndPortalOverride).apply { override(Blocks.END_PORTAL){ null } })
			register(BlockBrewingStandCustom(buildBrewingStand).apply { override(Blocks.BREWING_STAND){ ItemBlock(it, Item.Properties().group(ItemGroup.BREWING)) } })
			register(BlockDragonEggOverride(buildDragonEgg).apply { override(Blocks.DRAGON_EGG){ ItemDragonEgg(it, itemBlockDefaultProps) } })
			
			for(block in BlockShulkerBoxOverride.ALL_BLOCKS){
				register(BlockShulkerBoxOverride(Block.Properties.from(block), block.color).apply {
					override(block){ ItemShulkerBoxOverride(it, Item.Properties().maxStackSize(1).group(ItemGroup.DECORATIONS)) }
				})
			}
		}
	}
	
	@SubscribeEvent
	fun onRegisterItemBlocks(e: RegistryEvent.Register<Item>){
		temporaryItemBlocks.forEach(e.registry::register)
		temporaryItemBlocks.clear()
		
		// fire
		
		with(Blocks.FIRE as BlockFire){
			setFireInfo(WHITEBARK_LOG, 5, 5)
			setFireInfo(WHITEBARK, 5, 5)
			setFireInfo(WHITEBARK_PLANKS, 5, 20)
			setFireInfo(WHITEBARK_STAIRS, 5, 20)
			setFireInfo(WHITEBARK_SLAB, 5, 20)
			
			setFireInfo(WHITEBARK_LEAVES_AUTUMN_RED, 30, 60)
			setFireInfo(WHITEBARK_LEAVES_AUTUMN_BROWN, 30, 60)
			setFireInfo(WHITEBARK_LEAVES_AUTUMN_ORANGE, 30, 60)
			setFireInfo(WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN, 30, 60)
			
			setFireInfo(INFUSED_TNT, 15, 100)
			
			setFireInfo(ANCIENT_COBWEB, 100, 300)
			setFireInfo(DRY_VINES, 100, 300)
		}
		
		// vanilla modifications
		
		Blocks.END_PORTAL_FRAME.asItem().group = null
	}
	
	// Utilities
	
	private val temporaryItemBlocks = mutableListOf<ItemBlock>()
	
	private inline fun Block.override(vanillaBlock: Block, itemBlockConstructor: ((Block) -> ItemBlock?)){
		this.useVanillaName(vanillaBlock)
		itemBlockConstructor(this)?.let { with(it) }
	}
	
	private infix fun Block.with(itemBlock: ItemBlock) = apply {
		if (Resource.isVanilla(this.registryName!!)){
			itemBlock.useVanillaName(this)
		}
		else{
			itemBlock.registryName = this.registryName
		}
		
		temporaryItemBlocks.add(itemBlock)
		(itemBlock.group as? OrderedCreativeTab)?.registerOrder(itemBlock)
	}
	
	private infix fun <T : Block> T.with(itemBlockConstructor: (T) -> ItemBlock): Block{
		return with(itemBlockConstructor(this))
	}
}
