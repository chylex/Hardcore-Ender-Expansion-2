package chylex.hee.game.item

import net.minecraft.block.Block
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.inventory.EquipmentSlotType.HEAD
import net.minecraft.item.ItemStack
import net.minecraft.item.WallOrFloorItem

class ItemBlockHead(floorBlock: Block, wallBlock: Block, properties: Properties) : WallOrFloorItem(floorBlock, wallBlock, properties) {
	override fun getEquipmentSlot(stack: ItemStack): EquipmentSlotType {
		return HEAD
	}
}
