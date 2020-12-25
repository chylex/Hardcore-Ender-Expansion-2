package chylex.hee.game.loot

import chylex.hee.HEE
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import chylex.hee.game.loot.rng.RandomRoundingValueRange
import chylex.hee.game.loot.tables.NoStackSplittingLootTable
import chylex.hee.game.loot.tables.StackSortingLootTable
import chylex.hee.proxy.Environment
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import com.google.common.collect.ImmutableMap
import net.minecraft.client.resources.ReloadListener
import net.minecraft.profiler.EmptyProfiler
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.world.storage.loot.ConstantRange
import net.minecraft.world.storage.loot.IRandomRange
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraft.world.storage.loot.RandomValueRange
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import java.util.Random

@SubscribeAllEvents(modid = HEE.ID)
object LootTablePatcher {
	@SubscribeEvent
	fun onServerStarting(e: FMLServerStartingEvent) {
		with(e.server.resourceManager) {
			addReloadListener(LootReloadListener)
			LootReloadListener.run(this)
		}
	}
	
	private object LootReloadListener : ReloadListener<Any>() {
		override fun prepare(manager: IResourceManager, profiler: IProfiler) = Unit
		
		override fun apply(data: Any, manager: IResourceManager, profiler: IProfiler) {
			val lootManager = Environment.getServer().lootTableManager
			val tables = lootManager.registeredLootTables.toMutableMap()
			
			for(key in tables.keys) {
				if (Resource.isCustom(key)) {
					tables[key] = processCustomTable(tables.getValue(key))
				}
			}
			
			lootManager.registeredLootTables = ImmutableMap.copyOf(tables)
		}
		
		fun run(manager: IResourceManager) {
			apply(Unit, manager, EmptyProfiler.INSTANCE)
		}
	}
	
	private val POOLS_FIELD = LootTable::class.java.declaredFields.first { it.type === List::class.java }.also { it.isAccessible = true }
	private val IS_FROZEN_FIELD = LootPool::class.java.getDeclaredField("isFrozen").also { it.isAccessible = true }
	
	internal val LootTable.poolsExt
		@Suppress("UNCHECKED_CAST")
		get() = POOLS_FIELD.get(this) as ArrayList<LootPool>
	
	private inline fun LootPool.unfreeze(work: (LootPool) -> Unit) {
		IS_FROZEN_FIELD.set(this, false)
		this.apply(work).freeze()
	}
	
	private fun processCustomTable(originalTable: LootTable): LootTable {
		val table = reconfigureLootTable(originalTable)
		
		for(pool in table.poolsExt) {
			if (!pool.name.contains('#')) {
				continue
			}
			
			for((key, value) in parseParameters(pool)) {
				when(key) {
					"rolls_type" -> {
						val parameters = value.split(';').iterator()
						val range = determineRange(pool.rolls)
						
						pool.unfreeze {
							it.setRolls(when(val type = parameters.next()) {
								"rounding" -> RandomRoundingValueRange(range)
								"biased"   -> RandomBiasedValueRange(range, parameters.next().toFloat(), parameters.next().toFloat())
								else       -> throw UnsupportedOperationException(type)
							})
						}
					}
					
					"sort_order" -> {
						if (table is StackSortingLootTable) {
							table.setSortOrder(pool.name, value.toInt())
						}
						else {
							throw UnsupportedOperationException(key)
						}
					}
					
					else -> throw UnsupportedOperationException(key)
				}
			}
		}
		
		return table
	}
	
	private fun reconfigureLootTable(table: LootTable): LootTable {
		val pools = table.poolsExt
		val configurationPool = pools.find { it.name.startsWith("table#") }
		
		if (configurationPool == null) {
			return table
		}
		
		pools.remove(configurationPool)
		
		var newTable = table
		
		for((key, value) in parseParameters(configurationPool)) {
			when(key) {
				"stack_splitting" -> {
					if (value == "off") {
						newTable = NoStackSplittingLootTable(table)
					}
				}
				
				"stack_sorting" -> {
					if (value == "on") {
						newTable = StackSortingLootTable(table)
					}
				}
				
				else -> throw UnsupportedOperationException(key)
			}
		}
		
		return newTable
	}
	
	private fun determineRange(range: IRandomRange): ClosedFloatingPointRange<Float> {
		return when(range) {
			is RandomValueRange -> range.min..range.max
			is ConstantRange    -> range.generateInt(Random(0L)).toFloat().let { it..it }
			else                -> throw UnsupportedOperationException("cannot determine range of type ${range.type}")
		}
	}
	
	private fun parseParameters(pool: LootPool): List<Pair<String, String>> {
		return pool.name.split("#").drop(1).filter { it.startsWith("hee:") }.map { it.substringAfter("hee:").split('=').let { (k, v) -> Pair(k, v) } }
	}
}
