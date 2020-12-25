package chylex.hee.game.mechanics.table.interfaces

import chylex.hee.game.block.entity.TileEntityTablePedestal
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import org.apache.commons.lang3.math.Fraction

interface ITableContext {
	val isPaused: Boolean
	fun ensureDustAvailable(amount: Fraction): Boolean
	fun requestUseResources(): Boolean
	fun requestUseSupportingItem(getRequiredAmount: (ItemStack) -> Int): Pair<BlockPos, ItemStack>?
	fun getOutputPedestal(candidate: TileEntityTablePedestal): TileEntityTablePedestal
	fun triggerWorkParticle()
	fun markProcessFinished()
	
	@JvmDefault
	fun requestUseSupportingItem(item: Item, amount: Int): Pair<BlockPos, ItemStack>? {
		return this.requestUseSupportingItem { if (it.item === item) amount else 0 }
	}
}
