package chylex.hee.game.item.infusion
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.getListOfEnums
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object InfusionTag{
	private const val TAG_NAME = "Infusions"
	
	fun hasAny(stackNBT: NBTTagCompound): Boolean{
		return stackNBT.hasKey(TAG_NAME)
	}
	
	fun hasAny(stack: ItemStack): Boolean{
		val tag = stack.heeTagOrNull
		return tag != null && hasAny(tag)
	}
	
	fun getList(stackNBT: NBTTagCompound): InfusionList{
		return InfusionList(stackNBT.getListOfEnums(TAG_NAME))
	}
	
	fun getList(stack: ItemStack): InfusionList{
		return stack.heeTagOrNull?.let { getList(it) } ?: InfusionList.EMPTY
	}
	
	fun setList(stackNBT: NBTTagCompound, infusions: InfusionList){
		if (infusions.isEmpty){
			stackNBT.removeTag(TAG_NAME)
		}
		else{
			stackNBT.setList(TAG_NAME, infusions.tag)
		}
	}
	
	fun setList(stack: ItemStack, infusions: InfusionList){
		setList(stack.heeTag, infusions)
	}
}
