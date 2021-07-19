package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.CustomPlantType
import chylex.hee.game.block.properties.IBlockStateModel
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BushBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.PlantType
import net.minecraftforge.common.Tags

open class BlockEndPlant(builder: BlockBuilder) : BushBlock(builder.p), IHeeBlock {
	override val model: IBlockStateModel
		get() = BlockStateModels.Cross
	
	final override val renderLayer
		get() = CUTOUT
	
	override fun isValidGround(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return state.block.let { it === Blocks.END_STONE || it === Blocks.GRASS_BLOCK || Tags.Blocks.DIRT.contains(it) }
	}
	
	override fun getPlantType(world: IBlockReader, pos: BlockPos): PlantType {
		return CustomPlantType.END
	}
}
