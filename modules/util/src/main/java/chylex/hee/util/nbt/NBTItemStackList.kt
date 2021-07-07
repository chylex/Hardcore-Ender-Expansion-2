package chylex.hee.util.nbt

import net.minecraft.item.ItemStack

class NBTItemStackList(tagList: TagList = TagList()) : NBTList<ItemStack>(tagList) {
	override fun convert(element: ItemStack): NBTBase {
		return TagCompound().also { it.writeStack(element) }
	}
	
	override fun get(index: Int): ItemStack {
		return tagList.getCompound(index).readStack()
	}
	
	companion object {
		fun of(elements: Iterable<ItemStack>) = NBTItemStackList().apply { elements.forEach(::append) }
	}
}
