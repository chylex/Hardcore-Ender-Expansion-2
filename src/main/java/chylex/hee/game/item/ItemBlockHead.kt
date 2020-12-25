package chylex.hee.game.item

import chylex.hee.system.migration.ItemWallOrFloor
import net.minecraft.block.Block
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.inventory.EquipmentSlotType.HEAD
import net.minecraft.item.ItemStack

class ItemBlockHead(floorBlock: Block, wallBlock: Block, properties: Properties) : ItemWallOrFloor(floorBlock, wallBlock, properties) {
	override fun getEquipmentSlot(stack: ItemStack): EquipmentSlotType {
		return HEAD
	}
}
