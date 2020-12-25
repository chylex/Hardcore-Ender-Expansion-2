package chylex.hee.game.loot.conditions

import chylex.hee.system.facades.Resource
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameter
import net.minecraft.world.storage.loot.LootParameters

class ConditionFortune(override val minLevel: Int, override val maxLevel: Int) : ILootConditionWithRange {
	override fun test(context: LootContext): Boolean {
		return (context.get(LootParameters.TOOL)?.let { EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, it) } ?: 0) in minLevel..maxLevel
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.TOOL)
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionFortune>(Resource.Custom("fortune"), ConditionFortune::class.java) {
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionFortune(minLevel, maxLevel)
	}
}
