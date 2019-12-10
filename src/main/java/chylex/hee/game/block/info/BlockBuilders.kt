package chylex.hee.game.block.info
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.system.util.clone
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

object BlockBuilders{
	
	// Building (Uncategorized)
	
	val buildEtherealLantern = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.PURPLE, SoundType.GLASS).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.9F
		lightLevel = 15
	}
	
	val buildGraveDirt = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.DIRT, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.25F
	}
	
	val buildInfusedGlass = BlockBuilder(Material.GLASS, MapColor.ORANGE_STAINED_HARDENED_CLAY,SoundType.GLASS ).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.5F
		explosionResistance = 0.6F
	}
	
	val buildVantablock = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.BLACK, SoundType.CLOTH).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 10.0F
		explosionResistance = 1.0F
	}
	
	val buildEndiumBlock = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.BLUE, SoundType.METAL).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 6.2F
		explosionResistance = 20.0F
	}
	
	val buildEnderSol = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.WOOD, SoundType.GROUND.clone(pitch = 0.85F)).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.9F
	}
	
	val buildHumus = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.BLACK, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.3F
	}
	
	// Building (Gloomrock)
	
	val buildGloomrock = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.BLACK, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 7.0F
	}
	
	val buildGloomrockBricks = buildGloomrock.clone {
		harvestHardness = 2.8F
		explosionResistance = 10.0F
	}
	
	val buildGloomrockSmooth = buildGloomrock.clone {
		harvestHardness = 2.0F
		explosionResistance = 8.0F
	}
	
	val buildGloomtorch = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.BLACK, SoundType.STONE).apply {
		explosionResistance = 0.5F
		lightLevel = 13
	}
	
	// Building (Dusty Stone)
	
	val buildDustyStone = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.SAND, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE) // + shovel
		harvestHardness = 1.1F
		explosionResistance = 0.5F
	}
	
	val buildDustyStoneCracked = buildDustyStone.clone {
		harvestHardness = 1.0F
		explosionResistance = 0.2F
	}
	
	val buildDustyStoneDamaged = buildDustyStone.clone {
		harvestHardness = 0.9F
		explosionResistance = 0.1F
	}
	
	val buildDustyStoneBricks = buildDustyStone.clone {
		harvestHardness = 1.9F
		explosionResistance = 3.0F
	}
	
	// Building (Obsidian)
	
	val buildObsidian = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.BLACK, SoundType.STONE).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 50F
		explosionResistance = 2000F
	}
	
	val buildObsidianVariation = buildObsidian.clone {
		harvestHardness = 20F
		explosionResistance = 500F
	}
	
	val buildObsidianVariationLit = buildObsidianVariation.clone {
		lightLevel = 15
	}
	
	// Building (End Stone)
	
	val buildEndStone = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.SAND, SoundType.STONE).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 3.0F
		explosionResistance = 15.0F
	}
	
	// Building (Dark Loam)
	
	val buildDarkLoam = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.BLACK, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.6F
	}
	
	// Building (Wood)
	
	val buildWhitebark = BlockBuilder(Material.WOOD, MapColor.SNOW, SoundType.WOOD).apply {
		harvestTool = Pair(WOOD, AXE)
		harvestHardness = 2.0F
	}
	
	val buildWhitebarkPlanks = buildWhitebark.clone {
		explosionResistance = 5.0F
	}
	
	// Interactive (Storage)
	
	val buildJarODust = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.ORANGE_STAINED_HARDENED_CLAY, SoundType.METAL).apply {
		harvestHardness = 0.4F
		explosionResistance = 0F
	}
	
	val buildLootChest = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.BLACK, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 13
	}
	
	// Interactive (Puzzle)
	
	val buildPuzzleLogic = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.ORANGE_STAINED_HARDENED_CLAY, SoundType.STONE).apply {
		makeIndestructible()
	}
	
	val buildPuzzleWall = buildPuzzleLogic.clone {
		lightLevel = 14
	}
	
	// Interactive (Gates)
	
	val buildExperienceGate = BlockBuilder(Materials.SOLID_NO_TOOL, MapColor.GREEN, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 5
	}
	
	// Interactive (Uncategorized)
	
	val buildIgneousPlate = BlockBuilder(Materials.IGNEOUS_ROCK_PLATE, MapColor.AIR, SoundType.STONE)
	
	val buildBrewingStand = BlockBuilder(Material.IRON, MapColor.YELLOW, SoundType.STONE).apply {
		harvestHardness = 0.5F
		lightLevel = 2
	}
	
	// Ores
	
	private val buildEndOre = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.SAND, SoundType.STONE)
	
	val buildEndPowderOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.0F
		explosionResistance = 9.0F
	}
	
	val buildEndiumOre = buildEndOre.clone {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 5.0F
		explosionResistance = 16.5F
	}
	
	val buildStardustOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.8F
		explosionResistance = 14.0F
	}
	
	val buildIgneousRockOre = buildEndOre.clone {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 6.0F
	}
	
	// Decorative (Plants)
	
	val buildFlowerPot = BlockBuilder(Material.CIRCUITS, MapColor.AIR, SoundType.STONE).apply {
		harvestHardness = 0F
	}
	
	// Spawners
	
	val buildSpawnerObsidianTowers = BlockBuilder(Material.ROCK, MapColor.STONE, SoundType.METAL).apply {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 8.0F
		miningStats = false
	}
	
	// Portals
	
	val buildPortalInner = BlockBuilder(Material.PORTAL, MapColor.BLACK, SoundType.STONE).apply {
		makeIndestructible()
		lightLevel = 15
	}
	
	val buildPortalFrame = BlockBuilder(Material.PORTAL, MapColor.SAND, SoundType.STONE).apply {
		makeIndestructible()
	}
	
	val buildPortalFrameCrafted = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.SAND, SoundType.STONE).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 1.7F
	}
	
	// Energy
	
	val buildEnergyCluster = BlockBuilder(Materials.ENERGY_CLUSTER, MapColor.SNOW, SoundType.GLASS.clone(volume = 1.25F, pitch = 1.35F)).apply {
		lightLevel = 13
		lightOpacity = 0
	}
	
	val buildCorruptedEnergy = BlockBuilder(Materials.CORRUPTED_ENERGY, MapColor.PURPLE, SoundType.SAND)
	
	// Tables
	
	val buildTablePedestal = buildGloomrock.clone {
		harvestHardness *= 0.75F
		explosionResistance *= 0.5F
	}
	
	val buildTable = BlockBuilder(Materials.SOLID_WITH_TOOL, MapColor.GRAY, SoundType.METAL).apply {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 20.0F
		explosionResistance = 25.0F
	}
	
	// Utilities
	
	val buildEternalFire = BlockBuilder(Material.FIRE, MapColor.AIR, SoundType.CLOTH).apply {
		harvestHardness = 0F
		miningStats = false
		lightLevel = 15
	}
	
	val buildScaffolding = BlockBuilder(Materials.SCAFFOLDING, MapColor.AIR, SoundType.STONE).apply {
		makeIndestructible()
		lightOpacity = 0
	}
}
