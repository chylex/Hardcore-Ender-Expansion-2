package chylex.hee.game.item.util

import chylex.hee.util.nbt.TagCompound
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

// Size

inline var ItemStack.size: Int
	get() = count
	set(value) {
		count = value
	}

inline val ItemStack.isNotEmpty
	get() = !isEmpty

val ItemStack.nullIfEmpty
	get() = this.takeIf(ItemStack::isNotEmpty)

// Copy

inline fun ItemStack.copyIf(predicate: (ItemStack) -> Boolean): ItemStack {
	return if (predicate(this))
		this.copy()
	else
		this
}

fun ItemStack.copyIfNotEmpty(): ItemStack {
	return this.copyIf(ItemStack::isNotEmpty)
}

// Damage

fun ItemStack.doDamage(amount: Int, owner: LivingEntity, hand: Hand) {
	this.damageItem(amount, owner) { it.sendBreakAnimation(hand) }
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
inline val ItemStack.nbt: TagCompound
	get() = this.orCreateTag

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, null is returned instead.
 */
inline val ItemStack.nbtOrNull: TagCompound?
	get() = this.tag

/**
 * Recursively deletes all empty compound tags in the ItemStack.
 */
fun ItemStack.cleanupNBT() {
	fun cleanupTag(tag: TagCompound) {
		tag.keySet().removeIf {
			val nested = tag.get(it) as? TagCompound
			nested != null && nested.apply(::cleanupTag).isEmpty
		}
	}
	
	val nbt = this.nbtOrNull
	
	if (nbt != null && nbt.apply(::cleanupTag).isEmpty) {
		this.tag = null
	}
}
