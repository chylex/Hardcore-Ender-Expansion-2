package chylex.hee.game.loot

import chylex.hee.game.loot.tables.NoStackSplittingLootTable
import chylex.hee.game.loot.tables.StackSortingLootTable
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.loot.LootParameterSet
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.functions.ILootFunction

object LootTablePatcher {
	@JvmStatic
	fun patch(type: String, parameterSet: LootParameterSet, pools: Array<LootPool>, functions: Array<ILootFunction>): LootTable {
		if (type == "custom_stack_sorting") {
			val stackOrder = Object2IntOpenHashMap<String>()
			patchPools(pools, stackOrder)
			return StackSortingLootTable(parameterSet, pools, functions, stackOrder)
		}
		
		if (type == "no_stack_splitting") {
			patchPools(pools, null)
			return NoStackSplittingLootTable(parameterSet, pools, functions)
		}
		
		throw UnsupportedOperationException("[LootTablePatcher] invalid patch type: $type")
	}
	
	private fun patchPools(pools: Array<LootPool>, stackOrder: Object2IntMap<String>?) {
		for (pool in pools) {
			if (!pool.name.contains('#')) {
				continue
			}
			
			for((key, value) in parseParameters(pool)) {
				when(key) {
					"sort_order" -> {
						if (stackOrder != null) {
							@Suppress("ReplacePutWithAssignment")
							stackOrder.put(pool.name, value.toInt())
						}
						else {
							throw UnsupportedOperationException(key)
						}
					}
					
					else -> throw UnsupportedOperationException(key)
				}
			}
		}
	}
	
	private fun parseParameters(pool: LootPool): List<Pair<String, String>> {
		return pool.name.split("#").drop(1).filter { it.startsWith("hee:") }.map { it.substringAfter("hee:").split('=').let { (k, v) -> Pair(k, v) } }
	}
}
