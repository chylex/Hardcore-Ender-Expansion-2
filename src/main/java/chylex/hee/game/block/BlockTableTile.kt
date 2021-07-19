package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.IBlockStateModel
import net.minecraftforge.registries.ForgeRegistries

class BlockTableTile<T : TileEntityBaseTable>(builder: BlockBuilder, name: String, tier: Int, firstTier: Int) : BlockAbstractTableTile<T>(builder, name, tier, firstTier) {
	override val model: IBlockStateModel
		get() = BlockModel.Table
	
	@Suppress("UNCHECKED_CAST")
	override fun createTileEntity() = ForgeRegistries.TILE_ENTITIES.getValue(Resource.Custom(name))!!.create()!! as T
}
