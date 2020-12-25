package chylex.hee.game.item.infusion

import net.minecraft.item.Item

/**
 * Describes an item which can be infused with one or more [Infusions][Infusion]. The interface must be applied to a class extending [Item].
 */
interface IInfusableItem {
	fun canApplyInfusion(infusion: Infusion): Boolean
}
