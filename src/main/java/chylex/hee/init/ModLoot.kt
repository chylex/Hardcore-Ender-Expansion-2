package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.NoStackSplittingLootTable
import chylex.hee.game.loot.StackSortingLootTable
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.game.loot.functions.FunctionInfuse
import chylex.hee.game.loot.functions.FunctionPickColoredGloomrock
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import chylex.hee.system.Resource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager
import net.minecraft.world.storage.loot.functions.LootFunctionManager
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
object ModLoot{
	lateinit var HUMUS_EXPLODED: BlockLootTable
	lateinit var ANCIENT_COBWEB: BlockLootTable
	lateinit var END_POWDER_ORE: BlockLootTable
	lateinit var STARDUST_ORE: BlockLootTable
	lateinit var IGNEOUS_ROCK_ORE: BlockLootTable
	lateinit var CHORUS_PLANT: BlockLootTable
	
	lateinit var SILVERFISH: ResourceLocation
	lateinit var ENDERMITE_NATURAL: ResourceLocation
	lateinit var ENDERMITE_INSTABILITY: ResourceLocation
	
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		LootConditionManager.registerCondition(ConditionLooting.Serializer)
		LootConditionManager.registerCondition(ConditionCriticalHit.Serializer)
		
		LootFunctionManager.registerFunction(FunctionInfuse.Serializer)
		LootFunctionManager.registerFunction(FunctionPickColoredGloomrock.Serializer)
		
		HUMUS_EXPLODED = registerBlock("humus_exploded")
		ANCIENT_COBWEB = registerBlock("ancient_cobweb")
		END_POWDER_ORE = registerBlock("end_powder_ore")
		STARDUST_ORE = registerBlock("stardust_ore")
		IGNEOUS_ROCK_ORE = registerBlock("igneous_rock_ore")
		CHORUS_PLANT = registerBlock("chorus_plant")
		
		SILVERFISH = registerEntity("silverfish")
		ENDERMITE_NATURAL = registerEntity("endermite_natural")
		ENDERMITE_INSTABILITY = registerEntity("endermite_instability")
	}
	
	private fun registerBlock(name: String): BlockLootTable{
		return BlockLootTable(LootTableList.register(Resource.Custom("blocks/$name")))
	}
	
	private fun registerEntity(name: String): ResourceLocation{
		return LootTableList.register(Resource.Custom("entities/$name"))
	}
	
	val LootTable.pools
		get() = LootTable::class.java.declaredFields.first { it.type === List::class.java }.also { it.isAccessible = true }.get(this) as ArrayList<LootPool>
	
	@JvmStatic
	@SubscribeEvent
	fun onLootTableLoad(e: LootTableLoadEvent){
		if (e.name.namespace == HEE.ID){
			val table = reconfigureLootTable(e.table)
			
			for(pool in table.pools){
				if (!pool.name.contains('#')){
					continue
				}
				
				for((key, value) in parseParameters(pool)){
					when(key){
						"rolls_bias" -> {
							val (highestChanceValue, biasSoftener) = value.split('~')
							pool.rolls = RandomBiasedValueRange(pool.rolls, highestChanceValue.toFloat(), biasSoftener.toFloat())
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
		val pools = table.pools
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
