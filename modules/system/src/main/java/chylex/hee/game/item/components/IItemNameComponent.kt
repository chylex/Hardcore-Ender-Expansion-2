package chylex.hee.game.item.components

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent

interface IItemNameComponent {
	val defaultTranslationKey: String?
		get() = null
	
	fun getTranslationKey(stack: ItemStack): String? {
		return null
	}
	
	fun getDisplayName(stack: ItemStack): ITextComponent? {
		return null
	}
	
	companion object {
		fun of(translationKey: String) = object : IItemNameComponent {
			override val defaultTranslationKey
				get() = translationKey
		}
		
		fun of(item: Item) = object : IItemNameComponent {
			override val defaultTranslationKey
				get() = item.translationKey
		}
	}
}
