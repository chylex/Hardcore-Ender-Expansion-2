package chylex.hee.game.loot.conditions
import chylex.hee.system.util.facades.Resource
import net.minecraft.world.storage.loot.LootContext
import java.util.Random

class ConditionLooting(override val minLevel: Int, override val maxLevel: Int): ILootConditionWithRange{
	override fun testCondition(rand: Random, context: LootContext): Boolean{
		return context.lootingModifier in minLevel..maxLevel
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionLooting>(Resource.Custom("looting"), ConditionLooting::class.java){
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionLooting(minLevel, maxLevel)
	}
}
