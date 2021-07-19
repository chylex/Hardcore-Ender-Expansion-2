package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Block
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.inventory.EquipmentSlotType.HEAD
import net.minecraft.item.ItemStack
import net.minecraft.item.WallOrFloorItem

class ItemBlockHead(floorBlock: Block, wallBlock: Block, properties: Properties) : WallOrFloorItem(floorBlock, wallBlock, properties), IHeeItem {
	override val model
		get() = ItemModel.Skull
	
	override fun getEquipmentSlot(stack: ItemStack): EquipmentSlotType {
		return HEAD
	}
}
