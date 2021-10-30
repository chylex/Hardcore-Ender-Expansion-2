package chylex.hee.game.recipe.factories

import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.energy.IEnergyItem
import chylex.hee.game.recipe.factories.IngredientFullEnergy.Instance
import chylex.hee.system.getIfExists
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.network.PacketBuffer
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.IIngredientSerializer
import net.minecraftforge.registries.ForgeRegistries
import java.util.stream.Stream

object IngredientFullEnergy : IIngredientSerializer<Instance> {
	override fun write(buffer: PacketBuffer, ingredient: Instance) {
		buffer.writeRegistryId(ingredient.item)
	}
	
	override fun parse(buffer: PacketBuffer): Instance {
		return construct(buffer.readRegistryIdSafe(Item::class.java))
	}
	
	override fun parse(json: JsonObject): Instance {
		val itemName = JSONUtils.getString(json, "item")
		val item = ForgeRegistries.ITEMS.getIfExists(ResourceLocation(itemName))
		
		if (item == null) {
			throw JsonSyntaxException("Unknown item '$itemName'")
		}
		
		return construct(item)
	}
	
	private fun construct(item: Item): Instance {
		val energy = item.getHeeInterface<IEnergyItem>()
		if (energy == null) {
			throw JsonSyntaxException("Item '${item.registryName}' does not use Energy")
		}
		
		val stack = ItemStack(item).also {
			energy.setChargePercentage(it, 1F)
		}
		
		return Instance(stack, item, energy)
	}
	
	class Instance(stack: ItemStack, val item: Item, private val energy: IEnergyItem) : Ingredient(Stream.of(SingleItemList(stack))) {
		override fun test(ingredient: ItemStack?): Boolean {
			return ingredient != null && ingredient.item === item && energy.hasMaximumEnergy(ingredient) && !InfusionTag.hasAny(ingredient)
		}
		
		override fun getSerializer() = IngredientFullEnergy
	}
}
