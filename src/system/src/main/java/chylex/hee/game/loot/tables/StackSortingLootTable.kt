package chylex.hee.game.loot.tables

import chylex.hee.game.inventory.isNotEmpty
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameterSet
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.functions.ILootFunction
import java.util.Comparator.comparingInt
import java.util.function.Consumer

class StackSortingLootTable(parameterSet: LootParameterSet, pools: Array<out LootPool>, functions: Array<out ILootFunction>, orderMap: Object2IntMap<String>) : LootTable(parameterSet, pools, functions) {
	private val pools = pools.toList()
	
	private val orderMap = Object2IntOpenHashMap<LootPool>().apply {
		defaultReturnValue(0)
		
		for (entry in orderMap.object2IntEntrySet()) {
			@Suppress("ReplacePutWithAssignment")
			this.put(getPool(entry.key), entry.intValue) // kotlin indexer boxes the values
		}
	}
	
	override fun recursiveGenerate(context: LootContext, consumer: Consumer<ItemStack>) {
		if (context.addLootTable(this)) {
			val rand = context.random
			
			for(pool in pools.sortedWith(comparingInt(orderMap::getInt).thenComparing { _, _ -> if (rand.nextBoolean()) 1 else -1 })) {
				pool.generate(consumer, context)
			}
			
			context.removeLootTable(this)
		}
	}
	
	override fun fillInventory(inventory: IInventory, context: LootContext) {
		for((index, stack) in generate(context).filter(ItemStack::isNotEmpty).withIndex()) {
			inventory.setInventorySlotContents(index, stack)
		}
	}
}
