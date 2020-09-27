package chylex.hee.game.item
import chylex.hee.init.ModItems
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemTotemOfUndyingOverride(properties: Properties) : Item(properties){
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (!world.isRemote && entity is EntityPlayer){
			entity.replaceItemInInventory(itemSlot, ItemStack(ModItems.TOTEM_OF_UNDYING))
		}
	}
}
