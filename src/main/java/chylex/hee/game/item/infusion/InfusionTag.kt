package chylex.hee.game.item.infusion

import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfEnums
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putList
import net.minecraft.item.ItemStack

object InfusionTag {
	private const val TAG_NAME = "Infusions"
	
	// General
	
	fun hasAny(root: TagCompound): Boolean {
		return root.hasKey(TAG_NAME)
	}
	
	fun getList(root: TagCompound): InfusionList {
		return if (root.hasKey(TAG_NAME))
			InfusionList(root.getListOfEnums(TAG_NAME))
		else
			InfusionList.EMPTY
	}
	
	fun setList(root: TagCompound, infusions: InfusionList) {
		if (infusions.isEmpty) {
			root.remove(TAG_NAME)
		}
		else {
			root.putList(TAG_NAME, infusions.tag)
		}
	}
	
	// ItemStack
	
	fun hasAny(stack: ItemStack): Boolean {
		val tag = stack.heeTagOrNull
		return tag != null && hasAny(tag)
	}
	
	fun getList(stack: ItemStack): InfusionList {
		return stack.heeTagOrNull?.let(::getList) ?: InfusionList.EMPTY
	}
	
	fun setList(stack: ItemStack, infusions: InfusionList) {
		if (infusions.isEmpty) {
			stack.heeTagOrNull?.remove(TAG_NAME)
		}
		else {
			setList(stack.heeTag, infusions)
		}
	}
}
