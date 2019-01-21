package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBaseTable

class BlockTableTile<T : TileEntityBaseTable>(builder: BlockSimple.Builder, private val tileConstructor: () -> T, override val minAllowedTier: Int) : BlockAbstractTableTile<T>(builder){
	override fun createNewTileEntity() = tileConstructor()
}
