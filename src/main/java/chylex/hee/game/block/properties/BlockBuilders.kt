package chylex.hee.game.block.properties
import chylex.hee.game.block.clone
import chylex.hee.game.block.properties.BlockBuilder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.item.Tool.Level.DIAMOND
import chylex.hee.game.item.Tool.Level.IRON
import chylex.hee.game.item.Tool.Level.STONE
import chylex.hee.game.item.Tool.Level.WOOD
import chylex.hee.game.item.Tool.Type.AXE
import chylex.hee.game.item.Tool.Type.PICKAXE
import chylex.hee.game.item.Tool.Type.SHOVEL
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor

object BlockBuilders{
	
	// Building (Uncategorized)
	
	val buildEtherealLantern = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.BLUE_TERRACOTTA, SoundType.GLASS).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.9F
		explosionResistance = 0.9F
		lightLevel = 15
	}
	
	val buildGraveDirt = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.DIRT, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.25F
		explosionResistance = 1.25F
	}
	
	val buildInfusedGlass = BlockBuilder(Materials.INFUSED_GLASS, MaterialColor.ADOBE /* RENAME ORANGE */, SoundType.GLASS).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.5F
		explosionResistance = 0.3F
	}
	
	val buildVantablock = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.BLACK, SoundType.CLOTH).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 10.0F
		explosionResistance = 0.6F
	}
	
	val buildEndiumBlock = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.LAPIS, SoundType.METAL).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 6.2F
		explosionResistance = 12.0F
	}
	
	val buildEnderSol = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.WOOD, SoundType.GROUND.clone(pitch = 0.85F)).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.9F
		explosionResistance = 1.9F
	}
	
	val buildHumus = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.BLACK, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.3F
		explosionResistance = 0.3F
		randomTicks = true
	}
	
	// Building (Gloomrock)
	
	val buildGloomrock = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.BLACK, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 4.2F
	}
	
	val buildGloomrockBricks = buildGloomrock.clone {
		harvestHardness = 2.8F
		explosionResistance = 6.0F
	}
	
	val buildGloomrockSmooth = buildGloomrock.clone {
		harvestHardness = 2.0F
		explosionResistance = 4.8F
	}
	
	val buildGloomtorch = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.BLACK, SoundType.STONE).apply {
		explosionResistance = 0.3F
		lightLevel = 13
	}
	
	// Building (Dusty Stone)
	
	val buildDustyStone = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.DIRT, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE) // + shovel
		harvestHardness = 1.1F
		explosionResistance = 0.3F
	}
	
	val buildDustyStoneCracked = buildDustyStone.clone {
		harvestHardness = 1.0F
		explosionResistance = 0.12F
	}
	
	val buildDustyStoneDamaged = buildDustyStone.clone {
		harvestHardness = 0.9F
		explosionResistance = 0.06F
	}
	
	val buildDustyStoneBricks = buildDustyStone.clone {
		harvestHardness = 1.9F
		explosionResistance = 1.8F
	}
	
	// Building (Obsidian)
	
	val buildObsidian = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.BLACK, SoundType.STONE).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 50F
		explosionResistance = 1200F
	}
	
	val buildObsidianVariation = buildObsidian.clone {
		harvestHardness = 20F
		explosionResistance = 300F
	}
	
	val buildObsidianVariationLit = buildObsidianVariation.clone {
		lightLevel = 15
	}
	
	val buildObsidianTowerTop = buildObsidianVariationLit.clone {
		makeIndestructible()
	}
	
	// Building (End Stone)
	
	val buildEndStone = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.SAND, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 3.0F
		explosionResistance = 9.0F
	}
	
	// Building (Dark Loam)
	
	val buildDarkLoam = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.BLACK, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.6F
		explosionResistance = 0.6F
	}
	
	// Building (Wood)
	
	val buildWhitebark = BlockBuilder(Material.WOOD, MaterialColor.SNOW, SoundType.WOOD).apply {
		harvestTool = Pair(WOOD, AXE)
		harvestHardness = 2.0F
		explosionResistance = 2.0F
	}
	
	val buildWhitebarkPlanks = buildWhitebark.clone {
		explosionResistance = 3.0F
	}
	
	// Building (Miner's Burial)
	
	val buildMinersBurial = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.RED, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.6F
		explosionResistance = 120F
	}
	
	val buildMinersBurialIndestructible = buildMinersBurial.clone {
		makeIndestructible()
	}
	
	// Fluids
	
	val buildCauldron = BlockBuilder(Material.IRON, MaterialColor.STONE, SoundType.STONE).apply {
		harvestHardness = 2.0F
		explosionResistance = 2.0F
	}
	
	// Interactive (Storage)
	
	val buildJarODust = BlockBuilder(Materials.JAR_O_DUST, MaterialColor.ORANGE_TERRACOTTA, SoundType.METAL).apply {
		harvestHardness = 0.4F
		explosionResistance = 0F
	}
	
	val buildLootChest = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.BLACK, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 13
	}
	
	// Interactive (Puzzle)
	
	val buildPuzzleLogic = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.ADOBE /* RENAME ORANGE */, SoundType.STONE).apply {
		makeIndestructible()
	}
	
	val buildPuzzleWall = buildPuzzleLogic.clone {
		lightLevel = 14
	}
	
	// Interactive (Gates)
	
	val buildExperienceGate = BlockBuilder(Materials.SOLID_NO_TOOL, MaterialColor.GREEN, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 5
	}
	
	// Interactive (Uncategorized)
	
	val buildIgneousPlate = BlockBuilder(Materials.IGNEOUS_ROCK_PLATE, MaterialColor.AIR, SoundType.STONE)
	
	val buildBrewingStand = BlockBuilder(Material.IRON, MaterialColor.YELLOW, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = 0.5F
		explosionResistance = 0.5F
		lightLevel = 2
	}
	
	// Ores
	
	private val buildEndOre = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.SAND, SoundType.STONE)
	
	val buildEndPowderOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.0F
		explosionResistance = 5.4F
	}
	
	val buildEndiumOre = buildEndOre.clone {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 5.0F
		explosionResistance = 9.9F
	}
	
	val buildStardustOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.8F
		explosionResistance = 8.4F
	}
	
	val buildIgneousRockOre = buildEndOre.clone {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 3.6F
	}
	
	// Decorative (Trees)
	
	val buildWhitebarkSapling = BlockBuilder(Material.PLANTS, MaterialColor.FOLIAGE, SoundType.PLANT).apply {
		isSolid = false
		harvestHardness = 0F
		explosionResistance = 0F
	}
	
	val buildWhitebarkLeaves = BlockBuilder(Material.LEAVES, MaterialColor.AIR, SoundType.PLANT).apply {
		isSolid = false
		harvestHardness = 0.2F
		explosionResistance = 0.2F
	}
	
	// Decorative (Plants)
	
	val buildPlant = BlockBuilder(Material.PLANTS, MaterialColor.AIR, SoundType.PLANT).apply {
		isSolid = false
		harvestHardness = 0F
		explosionResistance = 0F
	}
	
	val buildFlowerPot = BlockBuilder(Material.MISCELLANEOUS, MaterialColor.AIR, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = 0F
		explosionResistance = 0F
	}
	
	// Decorative (Uncategorized)
	
	val buildAncientCobweb = BlockBuilder(Materials.ANCIENT_COBWEB, MaterialColor.WOOL, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = 0.2F
		explosionResistance = 0.2F
	}
	
	val buildDryVines = BlockBuilder(Material.TALL_PLANTS, MaterialColor.FOLIAGE, SoundType.PLANT).apply {
		isSolid = false
		harvestTool = Pair(WOOD, AXE)
		harvestHardness = 0.1F
		explosionResistance = 0.1F
	}
	
	val buildEndermanHead = BlockBuilder(Material.MISCELLANEOUS, MaterialColor.BLACK, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = 1.0F
		explosionResistance = 1.0F
	}
	
	// Spawners
	
	val buildSpawnerObsidianTowers = BlockBuilder(Material.ROCK, MaterialColor.STONE, SoundType.METAL).apply {
		isSolid = false
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 8.0F
		explosionResistance = 8.0F
		noDrops = true
	}
	
	// Portals
	
	val buildPortalInner = BlockBuilder(Material.PORTAL, MaterialColor.BLACK, SoundType.STONE).apply {
		makeIndestructible()
		isSolid = false
		lightLevel = 15
	}
	
	val buildPortalFrame = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.SAND, SoundType.STONE).apply {
		makeIndestructible()
	}
	
	val buildPortalFrameCrafted = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.SAND, SoundType.STONE).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 1.7F
		explosionResistance = 1.7F
	}
	
	// Energy
	
	val buildEnergyCluster = BlockBuilder(Materials.ENERGY_CLUSTER, MaterialColor.SNOW, SoundType.GLASS.clone(volume = 1.25F, pitch = 1.35F)).apply {
		isSolid = false
		lightLevel = 13
		noDrops = true
	}
	
	val buildCorruptedEnergy = BlockBuilder(Materials.CORRUPTED_ENERGY, MaterialColor.PURPLE, SoundType.SAND).apply {
		isSolid = false
		randomTicks = true // just to be safe
		noDrops = true
	}
	
	// Tables
	
	val buildTablePedestal = buildGloomrock.clone {
		harvestHardness *= 0.75F
		explosionResistance *= 0.5F
	}
	
	val buildTable = BlockBuilder(Materials.SOLID_WITH_TOOL, MaterialColor.GRAY, SoundType.METAL).apply {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 20.0F
		explosionResistance = 15.0F
	}
	
	// Utilities
	
	val buildEternalFire = BlockBuilder(Material.FIRE, MaterialColor.AIR, SoundType.CLOTH).apply {
		isSolid = false
		harvestHardness = 0F
		explosionResistance = 0F
		noDrops = true
		lightLevel = 15
	}
	
	val buildScaffolding = BlockBuilder(Materials.SCAFFOLDING, MaterialColor.AIR, SoundType.STONE).apply {
		makeIndestructible()
		isSolid = false
	}
	
	// Overrides
	
	val buildEndPortalOverride = BlockBuilder(Material.PORTAL, MaterialColor.BLACK, SoundType.STONE).apply {
		makeIndestructible()
		isSolid = false
		lightLevel = 15
		randomTicks = true
	}
	
	val buildDragonEgg = BlockBuilder(Material.DRAGON_EGG, MaterialColor.BLACK, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = INDESTRUCTIBLE_HARDNESS
		explosionResistance = 9F
		lightLevel = 2
	}
}
