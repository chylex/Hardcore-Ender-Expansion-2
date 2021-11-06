package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IFlammableBlockComponent
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.init.ModBlocks
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.ToolType.AXE

object BlockWhitebark : HeeBlockBuilder() {
	init {
		material = Material.WOOD
		color = MaterialColor.SNOW
		sound = SoundType.WOOD
		tool = BlockHarvestTool.optional(WOOD, AXE)
	}
	
	val BARK = HeeBlockBuilder {
		includeFrom(BlockWhitebark)
		
		model = BlockModel.Cube(ModBlocks.WHITEBARK_LOG.location)
		
		tags.add(BlockTags.LOGS)
		tags.add(BlockTags.LOGS_THAT_BURN)
		
		hardness = BlockHardness(hardnessAndResistance = 2F)
		
		components.flammability = IFlammableBlockComponent.of(flammability = 5, fireSpread = 5)
	}
	
	val PLANKS = HeeBlockBuilder {
		includeFrom(BlockWhitebark)
		
		tags.add(BlockTags.PLANKS)
		
		hardness = BlockHardness(hardness = 2F, resistance = 3F)
		
		components.flammability = IFlammableBlockComponent.of(flammability = 20, fireSpread = 5)
	}
}
