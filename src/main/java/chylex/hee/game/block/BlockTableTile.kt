package chylex.hee.game.block
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.info.BlockBuilder

class BlockTableTile<T : TileEntityBaseTable>(builder: BlockBuilder, private val tileConstructor: () -> T, override val minAllowedTier: Int) : BlockAbstractTableTile<T>(builder){
	override fun createTileEntity() = tileConstructor()
}
