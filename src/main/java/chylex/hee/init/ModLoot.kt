package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.loot.NoStackSplittingLootTable
import chylex.hee.game.loot.StackSortingLootTable
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.game.loot.conditions.ConditionSilkTouch
import chylex.hee.game.loot.conditions.ConditionWasExploded
import chylex.hee.game.loot.conditions.ConditionWasSheared
import chylex.hee.game.loot.functions.FunctionInfuse
import chylex.hee.game.loot.functions.FunctionPickColoredGloomrock
import chylex.hee.game.loot.functions.FunctionPickMusicDisk
import chylex.hee.game.loot.functions.FunctionPickUndreadGem
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import chylex.hee.game.loot.rng.RandomRoundingValueRange
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import com.google.common.collect.ImmutableMap
import net.minecraft.client.resources.ReloadListener
import net.minecraft.profiler.EmptyProfiler
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.ConstantRange
import net.minecraft.world.storage.loot.IRandomRange
import net.minecraft.world.storage.loot.ItemLootEntry
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraft.world.storage.loot.RandomValueRange
import net.minecraft.world.storage.loot.conditions.LootConditionManager
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion
import net.minecraft.world.storage.loot.functions.LootFunctionManager
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.registries.ForgeRegistries
import java.util.Random

@SubscribeAllEvents(modid = HEE.ID)
object ModLoot{
	fun initialize(){
		LootConditionManager.registerCondition(ConditionCriticalHit.Serializer)
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		LootConditionManager.registerCondition(ConditionLooting.Serializer)
		LootConditionManager.registerCondition(ConditionSilkTouch.Serializer)
		LootConditionManager.registerCondition(ConditionWasExploded.Serializer)
		LootConditionManager.registerCondition(ConditionWasSheared.Serializer)
		
		LootFunctionManager.registerFunction(FunctionInfuse.Serializer)
		LootFunctionManager.registerFunction(FunctionPickColoredGloomrock.Serializer)
		LootFunctionManager.registerFunction(FunctionPickMusicDisk.Serializer)
		LootFunctionManager.registerFunction(FunctionPickUndreadGem.Serializer)
	}
	
	@SubscribeEvent
	fun onServerStarting(e: FMLServerStartingEvent){
		with(e.server.resourceManager){
			addReloadListener(LootReloadListener)
			LootReloadListener.run(this)
		}
	}
	
	private object LootReloadListener : ReloadListener<Any>(){
		override fun prepare(manager: IResourceManager, profiler: IProfiler) = Unit
		
		override fun apply(data: Any, manager: IResourceManager, profiler: IProfiler){
			val lootManager = Environment.getServer().lootTableManager
			val tables = lootManager.registeredLootTables.toMutableMap()
			
			for(key in tables.keys){
				if (Resource.isCustom(key)){
					tables[key] = processCustomTable(key, tables.getValue(key))
				}
			}
			
			lootManager.registeredLootTables = ImmutableMap.copyOf(tables)
		}
		
		fun run(manager: IResourceManager){
			apply(Unit, manager, EmptyProfiler.INSTANCE)
		}
	}
	
	private val POOLS_FIELD = LootTable::class.java.declaredFields.first { it.type === List::class.java }.also { it.isAccessible = true }
	private val IS_FROZEN_FIELD = LootPool::class.java.getDeclaredField("isFrozen").also { it.isAccessible = true }
	
	val LootTable.poolsExt
		@Suppress("UNCHECKED_CAST")
		get() = POOLS_FIELD.get(this) as ArrayList<LootPool>
	
	private inline fun LootPool.unfreeze(work: (LootPool) -> Unit){
		IS_FROZEN_FIELD.set(this, false)
		this.apply(work).freeze()
	}
	
	private fun processCustomTable(name: ResourceLocation, originalTable: LootTable): LootTable{
		val table = reconfigureLootTable(originalTable)
		
		for((index, pool) in table.poolsExt.withIndex()){
			if (!pool.name.contains('#')){
				continue
			}
			
			for((key, value) in parseParameters(pool)){
				when(key){
					"auto" -> {
						val definition = value.split(';')
						
						val type = definition[0]
						val parameters = definition.drop(1).toMutableSet()
						
						if (type == "block"){
							val block = ForgeRegistries.BLOCKS.getValue(Resource.Custom(name.path.removePrefix("blocks/")))!!
							
							table.poolsExt[index] = with(LootPool.builder()){
								rolls(ConstantRange(1))
								addEntry(ItemLootEntry.builder(block))
								acceptCondition(SurvivesExplosion.builder())
								build()
							}
						}
						else{
							throw UnsupportedOperationException("$key=$type")
						}
						
						if (parameters.isNotEmpty()){
							throw UnsupportedOperationException("unused auto loot table parameters: ${parameters.joinToString(",")} where full definition is $value")
						}
					}
					
					"rolls_type" -> {
						val parameters = value.split(';').iterator()
						val range = determineRange(pool.rolls)
						
						pool.unfreeze {
							it.setRolls(when(val type = parameters.next()){
								"rounding" -> RandomRoundingValueRange(range)
								"biased" -> RandomBiasedValueRange(range, parameters.next().toFloat(), parameters.next().toFloat())
								else -> throw UnsupportedOperationException(type)
							})
						}
					}
					
					"sort_order" -> {
						if (table is StackSortingLootTable){
							table.setSortOrder(pool.name, value.toInt())
						}
						else{
							throw UnsupportedOperationException(key)
						}
					}
					
					else -> throw UnsupportedOperationException(key)
				}
			}
		}
		
		return table
	}
	
	private fun reconfigureLootTable(table: LootTable): LootTable{
		val pools = table.poolsExt
		val configurationPool = pools.find { it.name.startsWith("table#") }
		
		if (configurationPool == null){
			return table
		}
		
		pools.remove(configurationPool)
		
		var newTable = table
		
		for((key, value) in parseParameters(configurationPool)){
			when(key){
				"stack_splitting" -> {
					if (value == "off"){
						newTable = NoStackSplittingLootTable(table)
					}
				}
				
				"stack_sorting" -> {
					if (value == "on"){
						newTable = StackSortingLootTable(table)
					}
				}
				
				else -> throw UnsupportedOperationException(key)
			}
		}
		
		return newTable
	}
	
	private fun determineRange(range: IRandomRange): ClosedFloatingPointRange<Float>{
		return when(range){
			is RandomValueRange -> range.min..range.max
			is ConstantRange -> range.generateInt(Random(0L)).toFloat().let { it..it }
			else -> throw UnsupportedOperationException("cannot determine range of type ${range.type}")
		}
	}
	
	private fun parseParameters(pool: LootPool): Sequence<Pair<String, String>>{
		return pool.name.splitToSequence('#').drop(1).map { it.split('=').let { (k, v) -> Pair(k, v) } }
	}
}
