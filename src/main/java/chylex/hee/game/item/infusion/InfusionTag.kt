package chylex.hee.game.item.infusion
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.getListOfEnums
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.item.ItemStack

object InfusionTag{
	private const val TAG_NAME = "Infusions"
	
	fun hasAny(stack: ItemStack): Boolean{
		val tag = stack.heeTagOrNull
		return tag != null && tag.hasKey(TAG_NAME)
	}
	
	fun getList(stack: ItemStack): InfusionList{
		val tag = stack.heeTagOrNull
		
		return if (tag != null && tag.hasKey(TAG_NAME))
			InfusionList(tag.getListOfEnums(TAG_NAME))
		else
			InfusionList.EMPTY
	}
	
	fun setList(stack: ItemStack, infusions: InfusionList){
		if (infusions.isEmpty){
			stack.heeTagOrNull?.removeTag(TAG_NAME)
		}
		else{
			stack.heeTag.setList(TAG_NAME, infusions.tag)
		}
	}
}
