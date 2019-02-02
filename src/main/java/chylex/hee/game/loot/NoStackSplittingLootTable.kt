package chylex.hee.game.loot
import chylex.hee.init.ModLoot.pools
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootTable
import java.util.Random

class NoStackSplittingLootTable(wrapped: LootTable) : LootTable(wrapped.pools.toTypedArray()){
	override fun shuffleItems(stacks: MutableList<ItemStack>, emptySlotCount: Int, rand: Random){
		stacks.shuffle(rand)
	}
}
