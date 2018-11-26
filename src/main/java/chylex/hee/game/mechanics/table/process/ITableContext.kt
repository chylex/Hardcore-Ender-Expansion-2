package chylex.hee.game.mechanics.table.process
import chylex.hee.game.block.entity.TileEntityTablePedestal

interface ITableContext{
	fun requestUseResources(): Boolean
	fun getOutputPedestal(candidate: TileEntityTablePedestal) : TileEntityTablePedestal
	fun markProcessFinished()
}
