package chylex.hee.game.mechanics.table.process
import chylex.hee.game.block.entity.TileEntityTablePedestal

interface ITableContext{
	val isPaused: Boolean
	fun requestUseResources(): Boolean
	fun getOutputPedestal(candidate: TileEntityTablePedestal) : TileEntityTablePedestal
	fun triggerTickParticle()
	fun markProcessFinished()
}
