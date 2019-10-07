package chylex.hee.game.item.infusion
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getListOfEnums
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.item.ItemStack

object InfusionTag{
	private const val TAG_NAME = "Infusions"
	
	// General
	
	fun hasAny(root: TagCompound): Boolean{
		return root.hasKey(TAG_NAME)
	}
	
	fun getList(root: TagCompound): InfusionList{
		return if (root.hasKey(TAG_NAME))
			InfusionList(root.getListOfEnums(TAG_NAME))
		else
			InfusionList.EMPTY
	}
	
	fun setList(root: TagCompound, infusions: InfusionList){
		if (infusions.isEmpty){
			root.removeTag(TAG_NAME)
		}
		else{
			root.setList(TAG_NAME, infusions.tag)
		}
	}
	
	// ItemStack
	
	fun hasAny(stack: ItemStack): Boolean{
		val tag = stack.heeTagOrNull
		return tag != null && hasAny(tag)
	}
	
	fun getList(stack: ItemStack): InfusionList{
		return stack.heeTagOrNull?.let(::getList) ?: InfusionList.EMPTY
	}
	
	fun setList(stack: ItemStack, infusions: InfusionList){
		if (infusions.isEmpty){
			stack.heeTagOrNull?.removeTag(TAG_NAME)
		}
		else{
			setList(stack.heeTag, infusions)
		}
	}
}
