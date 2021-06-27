package chylex.hee.game.loot.tables

import net.minecraft.item.ItemStack
import net.minecraft.loot.LootParameterSet
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.functions.ILootFunction
import java.util.Random

class NoStackSplittingLootTable(parameterSet: LootParameterSet, pools: Array<out LootPool>, functions: Array<out ILootFunction>) : LootTable(parameterSet, pools, functions) {
	override fun shuffleItems(stacks: MutableList<ItemStack>, emptySlotCount: Int, rand: Random) {
		stacks.shuffle(rand)
	}
}
