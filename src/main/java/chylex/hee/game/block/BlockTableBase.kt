package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel

class BlockTableBase(builder: BlockBuilder, tier: Int, firstTier: Int) : BlockAbstractTable(builder, tier, firstTier) {
	override val model
		get() = BlockModel.Multi(
			BlockModel.WithTextures(BlockModel.Parent("table_tier_$tier", Resource.Custom("block/table")), mapOf(
				"particle" to Resource.Custom("block/table_base"),
				"bottom" to Resource.Custom("block/table_base"),
				"top" to Resource.Custom("block/table_base"),
				"side" to Resource.Custom("block/table_base_side_$tier"),
			)),
			BlockModel.WithTextures(BlockModel.FromParent(Resource.Custom("block/table_tier_$tier")), mapOf(
				"overlay_top" to Resource.Custom("block/transparent"),
				"overlay_side" to Resource.Custom("block/transparent"),
			))
		)
	
	override fun getTranslationKey(): String {
		return "block.hee.table_base"
	}
}
