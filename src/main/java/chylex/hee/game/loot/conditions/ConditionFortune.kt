package chylex.hee.game.loot.conditions
import chylex.hee.game.loot.BlockLootTable.BlockLootContext
import chylex.hee.system.Resource
import net.minecraft.world.storage.loot.LootContext
import java.util.Random

class ConditionFortune(override val minLevel: Int, override val maxLevel: Int): ILootConditionWithRange{
	override fun testCondition(rand: Random, context: LootContext): Boolean{
		return context is BlockLootContext && context.fortune in minLevel..maxLevel
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionFortune>(Resource.Custom("fortune"), ConditionFortune::class.java){
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionFortune(minLevel, maxLevel)
	}
}
