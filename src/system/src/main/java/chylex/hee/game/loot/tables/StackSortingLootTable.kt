package chylex.hee.game.loot.tables
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.loot.LootTablePatcher.poolsExt
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import java.util.Comparator.comparingInt
import java.util.function.Consumer

class StackSortingLootTable(wrapped: LootTable) : LootTable(wrapped.parameterSet, wrapped.poolsExt.toTypedArray(), wrapped.functions){
	private val orderMap = Object2IntOpenHashMap<LootPool>().apply { defaultReturnValue(0) }
	
	fun setSortOrder(poolName: String, order: Int){
		val pool = getPool(poolName)
		
		@Suppress("SENSELESS_COMPARISON")
		if (pool == null){
			throw NoSuchElementException()
		}
		
		orderMap[pool] = order
	}
	
	override fun recursiveGenerate(context: LootContext, consumer: Consumer<ItemStack>){
		if (context.addLootTable(this)){
			val rand = context.random
			
			for(pool in poolsExt.sortedWith(comparingInt(orderMap::getInt).thenComparing { _, _ -> if (rand.nextBoolean()) 1 else -1 })){
				pool.generate(consumer, context)
			}
			
			context.removeLootTable(this)
		}
	}
	
	override fun fillInventory(inventory: IInventory, context: LootContext){
		for((index, stack) in generate(context).filter { it.isNotEmpty }.withIndex()){
			inventory.setInventorySlotContents(index, stack)
		}
	}
}
