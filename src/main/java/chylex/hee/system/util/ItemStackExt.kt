package chylex.hee.system.util
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

// Size

inline var ItemStack.size: Int
	get() = count
	set(value){ count = value }

inline val ItemStack.isNotEmpty
	get() = !isEmpty

// Copy

inline fun ItemStack.copyIf(predicate: (ItemStack) -> Boolean): ItemStack{
	return if (predicate(this))
		this.copy()
	else
		this
}

// Enchantments

inline val ItemStack.enchantmentMap: Map<Enchantment, Int>
	get() = EnchantmentHelper.getEnchantments(this)

inline val ItemStack.enchantmentList: List<Pair<Enchantment, Int>>
	get() = this.enchantmentMap.toList()

// NBT

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, it will be created.
 */
inline val ItemStack.nbt: NBTTagCompound
	get() = this.tagCompound ?: NBTTagCompound().also { this.tagCompound = it }

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, null is returned instead.
 */
inline val ItemStack.nbtOrNull: NBTTagCompound?
	get() = this.tagCompound

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, they will be created.
 */
val ItemStack.heeTag: NBTTagCompound
	get() = this.nbt.heeTag

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, null is returned instead.
 */
val ItemStack.heeTagOrNull: NBTTagCompound?
	get() = this.nbtOrNull?.heeTagOrNull

/**
 * Recursively deletes all empty compound tags in the ItemStack.
 */
fun ItemStack.cleanupNBT(){
	fun cleanupTag(tag: NBTTagCompound){
		tag.keySet.removeIf {
			val nested = tag.getTag(it) as? NBTTagCompound
			nested != null && nested.apply(::cleanupTag).isEmpty
		}
	}
	
	val nbt = this.nbtOrNull
	
	if (nbt != null && nbt.apply(::cleanupTag).isEmpty){
		this.tagCompound = null
	}
}
