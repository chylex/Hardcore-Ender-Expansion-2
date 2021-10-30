package chylex.hee.game.item

import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.EnchantmentGlintComponent
import chylex.hee.game.item.components.IItemGlintComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.InfusionTag
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent

class ItemAbstractInfusable(infusable: IInfusableItem = IInfusableItem.Default) : HeeItemBuilder() {
	init {
		components.tooltip.add(ITooltipComponent { lines, stack, _, _ ->
			if (lines.size > 1) { // first line is item name
				lines.add(StringTextComponent(""))
			}
			
			lines.add(TranslationTextComponent("hee.infusions.list.title"))
			
			if (InfusionTag.hasAny(stack)) {
				for (infusion in InfusionTag.getList(stack)) {
					lines.add(TranslationTextComponent("hee.infusions.list.item", TranslationTextComponent(infusion.translationKey)))
				}
			}
			else {
				lines.add(TranslationTextComponent("hee.infusions.list.none"))
			}
		})
		
		// POLISH use a custom milder and slower texture
		components.glint = IItemGlintComponent.either(EnchantmentGlintComponent, InfusionTag::hasAny)
		
		interfaces[IInfusableItem::class.java] = infusable
	}
}
