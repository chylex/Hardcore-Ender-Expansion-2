package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.init.ModItems
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

object ItemTotemOfUndyingOverride : HeeItemBuilder() {
	init {
		localization = LocalizationStrategy.None
		model = ItemModel.Manual
		
		maxStackSize = 1
		
		components.tickInInventory.add(ITickInInventoryComponent { world, entity, _, slot, _ ->
			if (!world.isRemote && entity is PlayerEntity) {
				entity.replaceItemInInventory(slot, ItemStack(ModItems.TOTEM_OF_UNDYING))
			}
		})
	}
}
