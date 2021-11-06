package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockExperienceComponent
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.util.random.nextInt
import net.minecraftforge.common.ToolType.PICKAXE

object BlockEndPowderOre : HeeBlockBuilder() {
	init {
		includeFrom(BlockEndOre)
		
		drop = BlockDrop.Manual
		
		tool = BlockHarvestTool.required(STONE, PICKAXE)
		hardness = BlockHardness(hardness = 2F, resistance = 5.4F)
		
		components.experience = IBlockExperienceComponent { rand -> rand.nextInt(1, 2) }
	}
}
