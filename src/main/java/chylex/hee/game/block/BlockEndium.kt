package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockHarvestabilityComponent
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.util.forge.EventResult
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.item.Items
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraftforge.common.Tags
import net.minecraftforge.common.ToolType.PICKAXE

abstract class BlockEndium : HeeBlockBuilder() {
	init {
		material = Materials.SOLID
		tool = BlockHarvestTool.required(IRON, PICKAXE)
		
		components.harvestability = IBlockHarvestabilityComponent {
			if (it.getHeldItem(MAIN_HAND).item === Items.GOLDEN_PICKAXE)
				EventResult.ALLOW
			else
				EventResult.DEFAULT
		}
	}
	
	object Ore : BlockEndium() {
		init {
			includeFrom(BlockEndOre)
			
			hardness = BlockHardness(hardness = 5F, resistance = 9.9F)
		}
	}
	
	object Block : BlockEndium() {
		init {
			color = MaterialColor.LAPIS
			sound = SoundType.METAL
			
			tags.add(Tags.Blocks.STORAGE_BLOCKS)
			
			hardness = BlockHardness(hardness = 6.2F, resistance = 12F)
		}
	}
}
