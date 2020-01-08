package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.loot.NoStackSplittingLootTable
import chylex.hee.game.loot.StackSortingLootTable
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.game.loot.functions.FunctionInfuse
import chylex.hee.game.loot.functions.FunctionPickColoredGloomrock
import chylex.hee.game.loot.functions.FunctionPickMusicDisk
import chylex.hee.game.loot.functions.FunctionPickUndreadGem
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import chylex.hee.game.loot.rng.RandomRoundingValueRange
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraft.world.storage.loot.RandomValueRange
import net.minecraft.world.storage.loot.conditions.LootConditionManager
import net.minecraft.world.storage.loot.functions.LootFunctionManager
import net.minecraftforge.event.LootTableLoadEvent

@SubscribeAllEvents(modid = HEE.ID)
object ModLoot{
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		LootConditionManager.registerCondition(ConditionLooting.Serializer)
		LootConditionManager.registerCondition(ConditionCriticalHit.Serializer)
		
		LootFunctionManager.registerFunction(FunctionInfuse.Serializer)
		LootFunctionManager.registerFunction(FunctionPickColoredGloomrock.Serializer)
		LootFunctionManager.registerFunction(FunctionPickMusicDisk.Serializer)
		LootFunctionManager.registerFunction(FunctionPickUndreadGem.Serializer)
	}
	
	private val POOLS_FIELD = LootTable::class.java.declaredFields.first { it.type === List::class.java }.also { it.isAccessible = true }
	
	val LootTable.poolsExt
		@Suppress("UNCHECKED_CAST")
		get() = POOLS_FIELD.get(this) as ArrayList<LootPool>
	
	@SubscribeEvent
	fun onLootTableLoad(e: LootTableLoadEvent){
		if (Resource.isCustom(e.name)){
			val table = reconfigureLootTable(e.table)
			
			for(pool in table.poolsExt){
				if (!pool.name.contains('#')){
					continue
				}
				
				for((key, value) in parseParameters(pool)){
					when(key){
						"rolls_type" -> {
							val parameters = value.split(';').iterator()
							val rolls = pool.rolls as RandomValueRange
							
							pool.setRolls(when(val type = parameters.next()){
								"rounding" -> RandomRoundingValueRange(rolls)
								"biased" -> RandomBiasedValueRange(rolls, parameters.next().toFloat(), parameters.next().toFloat())
								else -> throw UnsupportedOperationException(type)
							})
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
			
			e.table = table
		}
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
	
	private fun parseParameters(pool: LootPool): Sequence<Pair<String, String>>{
		return pool.name.splitToSequence('#').drop(1).map { it.split('=').let { (k, v) -> Pair(k, v) } }
	}
}
