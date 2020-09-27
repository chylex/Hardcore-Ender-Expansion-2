package chylex.hee.game.block
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.init.factory.TileEntityConstructors

class BlockTableTile<T : TileEntityBaseTable>(builder: BlockBuilder, name: String, private val tileEntity: Class<T>, tier: Int, firstTier: Int) : BlockAbstractTableTile<T>(builder, name, tier, firstTier){
	override fun createTileEntity() = TileEntityConstructors.get(tileEntity).get()!!
}
