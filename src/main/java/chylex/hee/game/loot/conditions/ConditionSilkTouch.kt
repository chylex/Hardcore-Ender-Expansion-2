package chylex.hee.game.loot.conditions
import chylex.hee.system.migration.vanilla.Enchantments
import chylex.hee.system.util.facades.Resource
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameter
import net.minecraft.world.storage.loot.LootParameters

class ConditionSilkTouch(override val expectedValue: Boolean): ILootConditionBoolean{
	override fun isTrue(context: LootContext): Boolean{
		return (context.get(LootParameters.TOOL)?.let { EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) } ?: 0) > 0
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>>{
		return mutableSetOf(LootParameters.TOOL)
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionSilkTouch>(Resource.Custom("has_silk_touch"), ConditionSilkTouch::class.java){
		override fun construct(expectedValue: Boolean) = ConditionSilkTouch(expectedValue)
	}
}
