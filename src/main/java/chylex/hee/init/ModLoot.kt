package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.system.Resource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager
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
		get() = LootTable::class.java.declaredFields.first { it.type === List::class.java }.also { it.isAccessible = true }.get(this) as List<LootPool>
	
	@JvmStatic
	@SubscribeEvent
	fun onLootTableLoad(e: LootTableLoadEvent){
		if (e.name.namespace == HEE.ID){
			var table = e.table
			var pools = table.pools
			
			for(index in pools.indices){
				val pool = pools[index]
				val name = pool.name
				
				if (!name.contains('#')){
					continue
				}
				
				val split = name.splitToSequence('#').drop(1).map { it.split('=').let { (k, v) -> Pair(k, v) } }
				
				for((key, value) in split){
					when(key){
						else -> throw UnsupportedOperationException(key)
					}
				}
			}
			
			e.table = table
		}
	}
}
