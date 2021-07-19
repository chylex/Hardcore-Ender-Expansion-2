package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.IBlockStateModel
import net.minecraft.block.RotatedPillarBlock

open class BlockPillarCustom(builder: BlockBuilder) : RotatedPillarBlock(builder.p), IHeeBlock {
	override val model: IBlockStateModel
		get() = BlockStateModels.Pillar
}
