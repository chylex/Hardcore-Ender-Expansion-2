package chylex.hee.game.item.infusion

import chylex.hee.game.item.interfaces.IItemInterface
import net.minecraft.item.Item

/**
 * Describes an item which can be infused with one or more [Infusions][Infusion]. The interface must be applied to a class extending [Item].
 */
interface IInfusableItem : IItemInterface {
	fun canApplyInfusion(target: Item, infusion: Infusion): Boolean
	
	object Default : IInfusableItem {
		override fun canApplyInfusion(target: Item, infusion: Infusion): Boolean {
			return infusion.targetItems.contains(target)
		}
	}
}
