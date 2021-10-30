package chylex.hee.game.item

import chylex.hee.client.util.MC
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.properties.CustomRarity
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemAbstractTrinket(trinket: ITrinketItem) : HeeItemBuilder() {
	private companion object {
		private const val LANG_TOOLTIP_IN_SLOT_CHARGED = "item.tooltip.hee.trinket.in_slot.charged"
		private const val LANG_TOOLTIP_IN_SLOT_UNCHARGED = "item.tooltip.hee.trinket.in_slot.uncharged"
		private const val LANG_TOOLTIP_NOT_IN_SLOT_CHARGED = "item.tooltip.hee.trinket.not_in_slot.charged"
		private const val LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED = "item.tooltip.hee.trinket.not_in_slot.uncharged"
	}
	
	init {
		localizationExtra[LANG_TOOLTIP_IN_SLOT_CHARGED] = "§aActive"
		localizationExtra[LANG_TOOLTIP_IN_SLOT_UNCHARGED] = "§cMust be recharged"
		localizationExtra[LANG_TOOLTIP_NOT_IN_SLOT_CHARGED] = "§cMust be placed in Trinket slot"
		localizationExtra[LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED] = "§cMust be charged and placed in Trinket slot"
		
		components.tooltip.add(object : ITooltipComponent {
			override fun add(lines: MutableList<ITextComponent>, stack: ItemStack, advanced: Boolean, world: World?) {
				val player = MC.player ?: return
				
				if (lines.size > 1) { // first line is item name
					lines.add(StringTextComponent(""))
				}
				
				val inSlot = TrinketHandler.isInTrinketSlot(player, stack)
				val isCharged = trinket.canPlaceIntoTrinketSlot(stack)
				
				lines.add(TranslationTextComponent(when {
					inSlot && isCharged  -> LANG_TOOLTIP_IN_SLOT_CHARGED
					inSlot && !isCharged -> LANG_TOOLTIP_IN_SLOT_UNCHARGED
					!inSlot && isCharged -> LANG_TOOLTIP_NOT_IN_SLOT_CHARGED
					else                 -> LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED
				}))
			}
		})
		
		components.rarity = CustomRarity.TRINKET
		
		interfaces[ITrinketItem::class.java] = trinket
		
		callbacks.add {
			@Suppress("DEPRECATION")
			require(maxStackSize == 1) { "trinket item must have a maximum stack size of 1" }
		}
	}
}
