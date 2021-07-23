package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextBiasedFloat
import chylex.hee.util.math.ceilToInt
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockStardustOre(builder: BlockBuilder) : HeeBlock(builder) {
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.WithTextures(BlockModel.FromParent(Resource.Custom("block/cube_overlay")), mapOf(
				"particle" to ModBlocks.STARDUST_ORE.location("_particle"),
				"base" to Blocks.END_STONE.location,
			))
		)
	
	override val renderLayer
		get() = CUTOUT
	
	override val drop
		get() = BlockDrop.Manual
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return (((world as? World)?.rand ?: RANDOM).nextBiasedFloat(4F) * 6F).ceilToInt()
	}
}
