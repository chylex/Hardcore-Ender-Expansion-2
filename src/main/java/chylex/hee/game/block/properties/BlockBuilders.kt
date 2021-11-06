package chylex.hee.game.block.properties

import chylex.hee.game.block.properties.BlockBuilder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.block.util.clone
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor

@Suppress("ConvertLambdaToReference")
object BlockBuilders {
	
	// Building (Uncategorized)
	
	val buildEtherealLantern = BlockBuilder(Materials.SOLID, MaterialColor.BLUE_TERRACOTTA, SoundType.GLASS).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.9F
		explosionResistance = 0.9F
		lightLevel = 15
	}
	
	val buildGraveDirt = BlockBuilder(Materials.SOLID, MaterialColor.DIRT, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.25F
		explosionResistance = 1.25F
	}
	
	val buildInfusedGlass = BlockBuilder(Materials.INFUSED_GLASS, MaterialColor.ADOBE /* RENAME ORANGE */, SoundType.GLASS).apply {
		isSolid = false
		requiresTool = true
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.5F
		explosionResistance = 0.3F
	}
	
	val buildVantablock = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.CLOTH).apply {
		requiresTool = true
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 10.0F
		explosionResistance = 0.6F
	}
	
	val buildEnderSol = BlockBuilder(Materials.SOLID, MaterialColor.WOOD, SoundType.GROUND.clone(pitch = 0.85F)).apply {
		requiresTool = true
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 1.9F
		explosionResistance = 1.9F
	}
	
	val buildHumus = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.GROUND).apply {
		harvestTool = Pair(WOOD, SHOVEL)
		harvestHardness = 0.3F
		explosionResistance = 0.3F
		randomTicks = true
	}
	
	// Building (Gloomrock)
	
	val buildGloomrock = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.STONE).apply {
		requiresTool = true
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 4.2F
	}
	
	val buildGloomtorch = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.STONE).apply {
		explosionResistance = 0.3F
		lightLevel = 13
	}
	
	// Building (Dusty Stone)
	
	val buildDustyStone = BlockBuilder(Materials.SOLID, MaterialColor.DIRT, SoundType.STONE).apply {
		suffocates = false // prevents sliding off the block
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
	
	val buildObsidian = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.STONE).apply {
		requiresTool = true
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
	
	// Building (Dark Loam)
	
	val buildDarkLoam = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.GROUND).apply {
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
	
	// Building (Miner's Burial)
	
	val buildMinersBurial = BlockBuilder(Materials.SOLID, MaterialColor.RED, SoundType.STONE).apply {
		requiresTool = true
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 0.6F
		explosionResistance = 120F
	}
	
	// Fluids
	
	val buildCauldron = BlockBuilder(Material.IRON, MaterialColor.STONE, SoundType.STONE).apply {
		harvestHardness = 2.0F
		explosionResistance = 2.0F
	}
	
	// Interactive (Storage)
	
	val buildLootChest = BlockBuilder(Materials.SOLID, MaterialColor.BLACK, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 13
	}
	
	// Interactive (Gates)
	
	val buildExperienceGate = BlockBuilder(Materials.SOLID, MaterialColor.GREEN, SoundType.METAL).apply {
		makeIndestructible()
		lightLevel = 5
	}
	
	// Interactive (Uncategorized)
	
	val buildBrewingStand = BlockBuilder(Material.IRON, MaterialColor.YELLOW, SoundType.STONE).apply {
		isSolid = false
		harvestHardness = 0.5F
		explosionResistance = 0.5F
		lightLevel = 2
	}
	
	// Ores
	
	private val buildEndOre = BlockBuilder(Materials.SOLID, MaterialColor.SAND, SoundType.STONE).apply {
		requiresTool = true
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
	
	// Energy
	
	val buildEnergyCluster = BlockBuilder(Materials.ENERGY_CLUSTER, MaterialColor.SNOW, SoundType.GLASS.clone(volume = 1.25F, pitch = 1.35F)).apply {
		isSolid = false
		lightLevel = 13
		noDrops = true
	}
	
	// Tables
	
	val buildTablePedestal = buildGloomrock.clone {
		harvestHardness *= 0.75F
		explosionResistance *= 0.5F
	}
	
	val buildTable = BlockBuilder(Materials.SOLID, MaterialColor.GRAY, SoundType.METAL).apply {
		requiresTool = true
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
