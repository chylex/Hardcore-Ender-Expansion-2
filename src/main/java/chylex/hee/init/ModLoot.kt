package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager

object ModLoot{
	lateinit var ANCIENT_COBWEB: BlockLootTable
	lateinit var END_POWDER_ORE: BlockLootTable
	lateinit var STARDUST_ORE: BlockLootTable
	lateinit var IGNEOUS_ROCK_ORE: BlockLootTable
	lateinit var CHORUS_PLANT: BlockLootTable
	
	lateinit var SILVERFISH: ResourceLocation
	
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		LootConditionManager.registerCondition(ConditionCriticalHit.Serializer)
		
		ANCIENT_COBWEB = registerBlock("ancient_cobweb")
		END_POWDER_ORE = registerBlock("end_powder_ore")
		STARDUST_ORE = registerBlock("stardust_ore")
		IGNEOUS_ROCK_ORE = registerBlock("igneous_rock_ore")
		CHORUS_PLANT = registerBlock("chorus_plant")
		
		SILVERFISH = registerEntity("silverfish")
	}
	
	private fun registerBlock(name: String): BlockLootTable{
		return BlockLootTable(LootTableList.register(ResourceLocation(HEE.ID, "blocks/$name")))
	}
	
	private fun registerEntity(name: String): ResourceLocation{
		return LootTableList.register(ResourceLocation(HEE.ID, "entities/$name"))
	}
}
