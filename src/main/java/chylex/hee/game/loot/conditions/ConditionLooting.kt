package chylex.hee.game.loot.conditions
import chylex.hee.system.facades.Resource
import net.minecraft.loot.LootContext

class ConditionLooting(override val minLevel: Int, override val maxLevel: Int): ILootConditionWithRange{
	override fun test(context: LootContext): Boolean{
		return context.lootingModifier in minLevel..maxLevel
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionLooting>(Resource.Custom("looting"), ConditionLooting::class.java){
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionLooting(minLevel, maxLevel)
	}
}
