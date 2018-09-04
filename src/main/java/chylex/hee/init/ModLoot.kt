package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.conditions.ConditionFortune
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager

object ModLoot{
	lateinit var ANCIENT_COBWEB: BlockLootTable
	
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		
		ANCIENT_COBWEB = registerBlock("ancient_cobweb")
	}
	
	private fun registerBlock(name: String): BlockLootTable{
		return BlockLootTable(LootTableList.register(ResourceLocation(HardcoreEnderExpansion.ID, "blocks/$name")))
	}
}
