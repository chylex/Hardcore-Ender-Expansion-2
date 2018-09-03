package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.loot.BlockLootTable
import chylex.hee.game.loot.conditions.ConditionFortune
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootTableList
import net.minecraft.world.storage.loot.conditions.LootConditionManager

object ModLoot{
	fun initialize(){
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
	}
}
