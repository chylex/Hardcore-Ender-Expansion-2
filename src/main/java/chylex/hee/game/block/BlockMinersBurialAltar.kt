package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.properties.BlockModel

object BlockMinersBurialAltar : HeeBlockBuilder() {
	init {
		includeFrom(BlockMinersBurialCube.INDESCRUCTIBLE)
		
		localization = LocalizationStrategy.ReplaceWords("Miners", "Miner's")
		model = BlockModel.Manual
		
		components.entity = IBlockEntityComponent(::TileEntityMinersBurialAltar)
	}
}
