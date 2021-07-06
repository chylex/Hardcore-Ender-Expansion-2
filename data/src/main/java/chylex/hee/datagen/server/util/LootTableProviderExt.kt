package chylex.hee.datagen.server.util

import chylex.hee.system.migration.BlockFlowerPot
import com.mojang.datafixers.util.Pair
import net.minecraft.block.Block
import net.minecraft.data.DataGenerator
import net.minecraft.data.LootTableProvider
import net.minecraft.data.loot.BlockLootTables
import net.minecraft.loot.LootParameterSet
import net.minecraft.loot.LootParameterSets
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTable.Builder
import net.minecraft.loot.ValidationTracker
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

abstract class BlockLootTableProvider(generator: DataGenerator) : LootTableProvider(generator) {
	protected abstract val consumer: RegistrationConsumer
	
	final override fun getName(): String {
		return "Block Loot Tables"
	}
	
	final override fun getTables(): MutableList<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> {
		return mutableListOf(Pair.of(Supplier(::consumer), LootParameterSets.BLOCK))
	}
	
	final override fun validate(map: MutableMap<ResourceLocation, LootTable>, tracker: ValidationTracker) {}
	
	protected abstract class RegistrationConsumer : BlockLootTables() {
		private val lootTables = mutableMapOf<ResourceLocation, Builder>()
		
		override fun addTables() {}
		
		final override fun accept(consumer: BiConsumer<ResourceLocation?, Builder?>) {
			addTables()
			
			for((location, table) in lootTables) {
				consumer.accept(location, table)
			}
			
			lootTables.clear()
		}
		
		final override fun registerLootTable(block: Block, table: Builder) {
			check(lootTables.put(block.lootTable, table) == null)
		}
		
		protected fun dropSelf(block: Block) {
			registerDropSelfLootTable(block)
		}
		
		protected fun dropOther(block: Block, drop: IItemProvider) {
			registerDropping(block, drop)
		}
		
		protected fun dropFunc(block: Block, func: (Block) -> Builder) {
			registerLootTable(block, func)
		}
		
		protected fun dropFlowerPot(block: BlockFlowerPot) {
			registerFlowerPot(block)
		}
		
		protected companion object {
			val withName = BlockLootTables::droppingWithName
		}
	}
}
