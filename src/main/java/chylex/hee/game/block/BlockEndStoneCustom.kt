package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import net.minecraft.block.Blocks
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.Tags

class BlockEndStoneCustom(builder: BlockBuilder, mapColor: MaterialColor) : HeeBlock(builder.clone { color = mapColor }) {
	override val model
		get() = BlockModel.WithTextures(
			BlockModel.CubeBottomTop(bottom = Blocks.END_STONE.location),
			mapOf("particle" to this.location("_top"))
		)
	
	override val tags
		get() = listOf(Tags.Blocks.END_STONES)
}
