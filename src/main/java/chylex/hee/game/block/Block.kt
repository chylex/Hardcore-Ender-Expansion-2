package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import chylex.hee.game.block.properties.Materials
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.Tags

val BlockIndestructible
	get() = HeeBlockBuilder {
		drop = BlockDrop.Nothing
		tool = BlockHarvestTool.NONE
		hardness = BlockHardness(hardness = -1F, resistance = 3600000F)
	}

val BlockEndStoneBase
	get() = HeeBlockBuilder {
		material = Materials.SOLID
		color = MaterialColor.SAND
		sound = SoundType.STONE
	}

val BlockEndOre
	get() = HeeBlockBuilder {
		includeFrom(BlockEndStoneBase)
		tags.add(Tags.Blocks.ORES)
	}

val BlockPortalFrameBase
	get() = HeeBlockBuilder {
		model = IBlockStateModelSupplier { BlockModel.PortalFrame(it, "plain") }
		
		material = Materials.SOLID
		color = MaterialColor.SAND
		sound = SoundType.STONE
		
		components.shape = IBlockShapeComponent.of(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0))
	}

val BlockPortalFrameIndestructible
	get() = HeeBlockBuilder {
		includeFrom(BlockPortalFrameBase)
		includeFrom(BlockIndestructible)
	}
