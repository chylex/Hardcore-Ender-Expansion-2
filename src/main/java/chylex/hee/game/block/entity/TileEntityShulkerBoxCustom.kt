package chylex.hee.game.block.entity

import chylex.hee.game.block.BlockShulkerBoxOverride.BoxSize
import chylex.hee.game.container.ContainerShulkerBox
import chylex.hee.init.ModTileEntities
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.migration.TileEntityShulkerBox
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.heeTagOrNull
import chylex.hee.system.serialization.putEnum
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.item.ItemStack
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent

class TileEntityShulkerBoxCustom : TileEntityShulkerBox() {
	companion object {
		const val BOX_SIZE_TAG = "BoxSize"
	}
	
	var boxSize by NotifyOnChange(BoxSize.LARGE) { newValue ->
		val newItems = NonNullList.withSize(newValue.slots, ItemStack.EMPTY)
		
		for(slot in 0 until newValue.slots) {
			newItems[slot] = items[slot]
		}
		
		items = newItems
	}
	
	override fun getType(): TileEntityType<*> {
		return ModTileEntities.SHULKER_BOX
	}
	
	// Container
	
	override fun getDefaultName(): ITextComponent {
		return TranslationTextComponent(boxSize.translationKey)
	}
	
	override fun getSlotsForFace(side: Direction): IntArray {
		return boxSize.slotIndices
	}
	
	override fun createMenu(id: Int, inventory: PlayerInventory): Container {
		return ContainerShulkerBox(id, inventory, this)
	}
	
	// Serialization
	
	private fun writeCustomNBT(nbt: TagCompound) {
		nbt.heeTag.putEnum(BOX_SIZE_TAG, boxSize)
	}
	
	private fun readCustomNBT(nbt: TagCompound) {
		boxSize = nbt.heeTagOrNull?.getEnum<BoxSize>(BOX_SIZE_TAG) ?: boxSize
	}
	
	override fun getUpdatePacket(): SUpdateTileEntityPacket {
		return SUpdateTileEntityPacket(pos, 0, TagCompound().also(::writeCustomNBT))
	}
	
	override fun onDataPacket(net: NetworkManager, packet: SUpdateTileEntityPacket) {
		readCustomNBT(packet.nbtCompound)
	}
	
	override fun getUpdateTag(): TagCompound {
		return super.write(TagCompound()).also(::writeCustomNBT)
	}
	
	override fun handleUpdateTag(nbt: TagCompound) {
		super.read(nbt)
		readCustomNBT(nbt)
	}
	
	override fun saveToNbt(nbt: TagCompound): TagCompound {
		return super.saveToNbt(nbt).also(::writeCustomNBT)
	}
	
	override fun loadFromNbt(nbt: TagCompound) {
		super.loadFromNbt(nbt)
		readCustomNBT(nbt)
	}
}
