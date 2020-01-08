package chylex.hee.game.item
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemTotemOfUndyingOverride(properties: Properties, private val originalTranslationKey: String) : Item(properties){
	override fun getTranslationKey() = originalTranslationKey
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (!world.isRemote && entity is EntityPlayer){
			entity.replaceItemInInventory(itemSlot, ItemStack(ModItems.TOTEM_OF_UNDYING))
		}
	}
}
