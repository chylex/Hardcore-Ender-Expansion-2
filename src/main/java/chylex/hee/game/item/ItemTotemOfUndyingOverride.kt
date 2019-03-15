package chylex.hee.game.item
import chylex.hee.init.ModItems
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemTotemOfUndyingOverride : Item(){
	init{
		translationKey = "totem"
		maxStackSize = 1
	}
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (!world.isRemote && entity is EntityPlayer){
			entity.replaceItemInInventory(itemSlot, ItemStack(ModItems.TOTEM_OF_UNDYING))
		}
	}
}
