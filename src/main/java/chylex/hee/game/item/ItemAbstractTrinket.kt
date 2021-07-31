package chylex.hee.game.item

import chylex.hee.client.util.MC
import chylex.hee.game.item.properties.CustomRarity
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

open class ItemAbstractTrinket(properties: Properties) : HeeItem(properties), ITrinketItem {
	companion object {
		private const val LANG_TOOLTIP_IN_SLOT_CHARGED = "item.tooltip.hee.trinket.in_slot.charged"
		private const val LANG_TOOLTIP_IN_SLOT_UNCHARGED = "item.tooltip.hee.trinket.in_slot.uncharged"
		private const val LANG_TOOLTIP_NOT_IN_SLOT_CHARGED = "item.tooltip.hee.trinket.not_in_slot.charged"
		private const val LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED = "item.tooltip.hee.trinket.not_in_slot.uncharged"
		
		fun onGetRarity(): Rarity {
			return CustomRarity.TRINKET
		}
		
		@Sided(Side.CLIENT)
		fun onAddInformation(stack: ItemStack, trinket: ITrinketItem, lines: MutableList<ITextComponent>) {
			val player = MC.player ?: return
			
			if (lines.size > 1) { // first line is item name
				lines.add(StringTextComponent(""))
			}
			
			val inSlot = TrinketHandler.isInTrinketSlot(player, stack)
			val isCharged = trinket.canPlaceIntoTrinketSlot(stack)
			
			lines.add(TranslationTextComponent(when {
				inSlot && isCharged   -> LANG_TOOLTIP_IN_SLOT_CHARGED
				inSlot && !isCharged  -> LANG_TOOLTIP_IN_SLOT_UNCHARGED
				!inSlot && isCharged  -> LANG_TOOLTIP_NOT_IN_SLOT_CHARGED
				!inSlot && !isCharged -> LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED
				else                  -> return // ???
			}))
		}
	}
	
	override val localizationExtra
		get() = mapOf(
			LANG_TOOLTIP_IN_SLOT_CHARGED to "§aActive",
			LANG_TOOLTIP_IN_SLOT_UNCHARGED to "§cMust be recharged",
			LANG_TOOLTIP_NOT_IN_SLOT_CHARGED to "§cMust be placed in Trinket slot",
			LANG_TOOLTIP_NOT_IN_SLOT_UNCHARGED to "§cMust be charged and placed in Trinket slot",
		)
	
	init {
		@Suppress("DEPRECATION")
		require(maxStackSize == 1) { "trinket item must have a maximum stack size of 1" }
	}
	
	override fun getRarity(stack: ItemStack): Rarity {
		return onGetRarity()
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, this, lines)
	}
}
