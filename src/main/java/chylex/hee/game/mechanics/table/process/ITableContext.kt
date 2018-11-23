package chylex.hee.game.mechanics.table.process

interface ITableContext{
	fun requestUseResources(): Boolean
	fun markProcessFinished()
}
