package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.conditions.ConditionFortune
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager

object ModLoot{
	lateinit var ANCIENT_COBWEB: BlockLootTable
	lateinit var END_POWDER_ORE: BlockLootTable
	lateinit var STARDUST_ORE: BlockLootTable
	lateinit var IGNEOUS_ROCK_ORE: BlockLootTable
	
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		
		ANCIENT_COBWEB = registerBlock("ancient_cobweb")
		END_POWDER_ORE = registerBlock("end_powder_ore")
		STARDUST_ORE = registerBlock("stardust_ore")
		IGNEOUS_ROCK_ORE = registerBlock("igneous_rock_ore")
	}
	
	private fun registerBlock(name: String): BlockLootTable{
		return BlockLootTable(LootTableList.register(ResourceLocation(HardcoreEnderExpansion.ID, "blocks/$name")))
	}
}
