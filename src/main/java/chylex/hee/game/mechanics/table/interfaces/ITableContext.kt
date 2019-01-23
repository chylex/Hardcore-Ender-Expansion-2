package chylex.hee.game.mechanics.table.interfaces
import chylex.hee.game.block.entity.TileEntityTablePedestal

interface ITableContext{
	val isPaused: Boolean
	fun requestUseResources(): Boolean
	fun getOutputPedestal(candidate: TileEntityTablePedestal) : TileEntityTablePedestal
	fun triggerWorkParticle()
	fun markProcessFinished()
}
