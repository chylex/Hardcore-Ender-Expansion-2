package chylex.hee.game.loot
import chylex.hee.init.ModLoot.pools
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import java.util.Comparator.comparingInt
import java.util.Random

class StackSortingLootTable(wrapped: LootTable) : LootTable(wrapped.pools.toTypedArray()){
	private val orderMap = Object2IntOpenHashMap<LootPool>().apply { defaultReturnValue(0) }
	
	fun setSortOrder(poolName: String, order: Int){
		val pool = getPool(poolName)
		
		@Suppress("SENSELESS_COMPARISON")
		if (pool == null){
			throw NoSuchElementException()
		}
		
		orderMap[pool] = order
	}
	
	override fun generateLootForPools(rand: Random, context: LootContext): MutableList<ItemStack>{
		val list = mutableListOf<ItemStack>()
		
		if (context.addLootTable(this)){
			for(pool in pools.sortedWith(comparingInt(orderMap::getInt).thenComparing { _, _ -> if (rand.nextBoolean()) 1 else -1 })){
				pool.generateLoot(list, rand, context)
			}
			
			context.removeLootTable(this)
		}
		
		return list
	}
	
	override fun fillInventory(inventory: IInventory, rand: Random, context: LootContext){
		for((index, stack) in generateLootForPools(rand, context).withIndex()){
			inventory.setInventorySlotContents(index, stack)
		}
	}
}
