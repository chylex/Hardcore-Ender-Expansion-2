package chylex.hee.game.loot.modifiers

import chylex.hee.game.item.util.size
import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.LootContext
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.loot.GlobalLootModifierSerializer
import net.minecraftforge.common.loot.LootModifier

class ModifierHarvestPotatoesOnHumus(conditions: Array<out ILootCondition>) : LootModifier(conditions) {
	override fun doApply(generatedLoot: MutableList<ItemStack>, context: LootContext): MutableList<ItemStack> {
		val modifiedLoot = mutableListOf<ItemStack>()
		
		for (stack in generatedLoot) {
			if (stack.item === Items.POISONOUS_POTATO) {
				modifiedLoot.add(ItemStack(Items.POTATO, stack.size).apply { deserializeNBT(stack.serializeNBT()) })
			}
			else {
				modifiedLoot.add(stack)
			}
		}
		
		return modifiedLoot
	}
	
	object Serializer : GlobalLootModifierSerializer<ModifierHarvestPotatoesOnHumus>() {
		override fun read(location: ResourceLocation, json: JsonObject, conditions: Array<out ILootCondition>): ModifierHarvestPotatoesOnHumus {
			return ModifierHarvestPotatoesOnHumus(conditions)
		}
		
		override fun write(instance: ModifierHarvestPotatoesOnHumus): JsonObject {
			return makeConditions(instance.conditions)
		}
	}
}
