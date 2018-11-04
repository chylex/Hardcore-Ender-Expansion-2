package chylex.hee.system.util
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack

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
