package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.item.util.Tool.Level
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.ToolType

object BlockMinersBurialCube : HeeBlockBuilder() {
	init {
		localization = LocalizationStrategy.Parenthesized(LocalizationStrategy.DeleteWords(LocalizationStrategy.ReplaceWords("Miners", "Miner's"), "Plain"), wordCount = 1, wordOffset = 3, fromStart = true)
		
		material = Materials.SOLID
		color = MaterialColor.RED
		sound = SoundType.STONE
		
		tool = BlockHarvestTool.required(Level.WOOD, ToolType.PICKAXE)
		hardness = BlockHardness(hardness = 0.6F, resistance = 120F)
	}
	
	val INDESCRUCTIBLE = HeeBlockBuilder {
		includeFrom(BlockIndestructible)
		includeFrom(BlockMinersBurialCube)
	}
}
