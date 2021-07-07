package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.Resource
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.game.loot.conditions.ConditionPlantedOnHumus
import chylex.hee.game.loot.conditions.ConditionSilkTouch
import chylex.hee.game.loot.conditions.ConditionWasExploded
import chylex.hee.game.loot.conditions.ConditionWasSheared
import chylex.hee.game.loot.functions.FunctionInfuse
import chylex.hee.game.loot.functions.FunctionPickUndreadGem
import chylex.hee.game.loot.functions.FunctionSetItem
import chylex.hee.game.loot.modifiers.ModifierHarvestPotatoesOnHumus
import chylex.hee.game.loot.rng.RandomBiasedValueRange
import chylex.hee.game.loot.rng.RandomRoundingValue
import chylex.hee.system.named
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.loot.ILootSerializer
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootFunctionType
import net.minecraft.loot.RandomRanges
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.loot.functions.ILootFunction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.Registry
import net.minecraftforge.common.loot.GlobalLootModifierSerializer
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModLoot {
	val CONDITION_CRITICAL_HIT     = registerCondition(Resource.Custom("killed_by_critical_hit"), ConditionCriticalHit.Serializer)
	val CONDITION_FORTUNE          = registerCondition(Resource.Custom("fortune"), ConditionFortune.Serializer)
	val CONDITION_LOOTING          = registerCondition(Resource.Custom("looting"), ConditionLooting.Serializer)
	val CONDITION_PLANTED_ON_HUMUS = registerCondition(Resource.Custom("planted_on_humus"), ConditionPlantedOnHumus.Serializer)
	val CONDITION_SILK_TOUCH       = registerCondition(Resource.Custom("has_silk_touch"), ConditionSilkTouch.Serializer)
	val CONDITION_WAS_EXPLODED     = registerCondition(Resource.Custom("was_exploded"), ConditionWasExploded.Serializer)
	val CONDITION_WAS_SHEARED      = registerCondition(Resource.Custom("was_sheared"), ConditionWasSheared.Serializer)
	
	val FUNCTION_INFUSE           = registerFunction(Resource.Custom("infuse"), FunctionInfuse.Serializer)
	val FUNCTION_PICK_UNDREAD_GEM = registerFunction(Resource.Custom("pick_undread_gem"), FunctionPickUndreadGem.Serializer)
	val FUNCTION_SET_ITEM         = registerFunction(Resource.Custom("set_item"), FunctionSetItem.Serializer)
	
	init {
		RandomRanges.GENERATOR_MAP[RandomBiasedValueRange.LOCATION] = RandomBiasedValueRange::class.java
		RandomRanges.GENERATOR_MAP[RandomRoundingValue.LOCATION] = RandomRoundingValue::class.java
	}
	
	@SubscribeEvent
	fun registerModifiers(e: RegistryEvent.Register<GlobalLootModifierSerializer<*>>) {
		with(e.registry) {
			register(ModifierHarvestPotatoesOnHumus.Serializer named "harvest_potatoes_on_humus")
		}
	}
	
	private fun registerCondition(id: ResourceLocation, serializer: ILootSerializer<out ILootCondition>): LootConditionType {
		return Registry.register(Registry.LOOT_CONDITION_TYPE, id, LootConditionType(serializer))
	}
	
	private fun registerFunction(id: ResourceLocation, serializer: ILootSerializer<out ILootFunction>): LootFunctionType {
		return Registry.register(Registry.LOOT_FUNCTION_TYPE, id, LootFunctionType(serializer))
	}
}
