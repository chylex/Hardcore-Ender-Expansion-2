package chylex.hee.game.mechanics.table.interfaces
import chylex.hee.game.block.entity.TileEntityTablePedestal
import net.minecraft.item.Item

interface ITableContext{
	val isPaused: Boolean
	fun requestUseResources(): Boolean
	fun requestUseSupportingItem(item: Item, amount: Int): Boolean
	fun getOutputPedestal(candidate: TileEntityTablePedestal) : TileEntityTablePedestal
	fun triggerWorkParticle()
	fun markProcessFinished()
}
