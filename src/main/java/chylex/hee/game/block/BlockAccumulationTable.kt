package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityAccumulationTable

class BlockAccumulationTable(builder: BlockSimple.Builder) : BlockAbstractTableTile<TileEntityAccumulationTable>(builder){
	override fun createNewTileEntity() = TileEntityAccumulationTable()
}
