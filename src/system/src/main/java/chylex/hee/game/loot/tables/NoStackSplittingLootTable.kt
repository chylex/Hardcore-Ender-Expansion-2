package chylex.hee.game.loot.tables
import chylex.hee.game.loot.LootTablePatcher.poolsExt
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootTable
import java.util.Random

class NoStackSplittingLootTable(wrapped: LootTable) : LootTable(wrapped.parameterSet, wrapped.poolsExt.toTypedArray(), wrapped.functions){
	override fun shuffleItems(stacks: MutableList<ItemStack>, emptySlotCount: Int, rand: Random){
		stacks.shuffle(rand)
	}
}
