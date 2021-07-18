package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.CustomPlantType
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BushBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.PlantType
import net.minecraftforge.common.Tags

open class BlockEndPlant(builder: BlockBuilder) : BushBlock(builder.p), IBlockLayerCutout {
	override fun isValidGround(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return state.block.let { it === Blocks.END_STONE || it === Blocks.GRASS_BLOCK || Tags.Blocks.DIRT.contains(it) }
	}
	
	override fun getPlantType(world: IBlockReader, pos: BlockPos): PlantType {
		return CustomPlantType.END
	}
}
