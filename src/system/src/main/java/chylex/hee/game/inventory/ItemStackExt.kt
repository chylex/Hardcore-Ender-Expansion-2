package chylex.hee.game.inventory
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.heeTagOrNull
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

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

fun ItemStack.copyIfNotEmpty(): ItemStack {
	return this.copyIf(ItemStack::isNotEmpty)
}

// Enchantments

inline val ItemStack.enchantmentMap: Map<Enchantment, Int>
	get() = EnchantmentHelper.getEnchantments(this)

inline val ItemStack.enchantmentList: List<Pair<Enchantment, Int>>
	get() = this.enchantmentMap.toList()

// Damage

fun ItemStack.doDamage(amount: Int, owner: EntityLivingBase, hand: Hand){
	this.damageItem(amount, owner){ it.sendBreakAnimation(hand) }
}

// NBT

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, it will be created.
 */
inline val ItemStack.nbt: TagCompound
	get() = this.orCreateTag

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, null is returned instead.
 */
inline val ItemStack.nbtOrNull: TagCompound?
	get() = this.tag

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, they will be created.
 */
val ItemStack.heeTag: TagCompound
	get() = this.nbt.heeTag

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, null is returned instead.
 */
val ItemStack.heeTagOrNull: TagCompound?
	get() = this.nbtOrNull?.heeTagOrNull

/**
 * Recursively deletes all empty compound tags in the ItemStack.
 */
fun ItemStack.cleanupNBT(){
	fun cleanupTag(tag: TagCompound){
		tag.keySet().removeIf {
			val nested = tag.get(it) as? TagCompound
			nested != null && nested.apply(::cleanupTag).isEmpty
		}
	}
	
	val nbt = this.nbtOrNull
	
	if (nbt != null && nbt.apply(::cleanupTag).isEmpty){
		this.tag = null
	}
}
