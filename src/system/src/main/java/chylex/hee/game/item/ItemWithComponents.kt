package chylex.hee.game.item

import chylex.hee.game.item.components.UseOnBlockComponent
import chylex.hee.system.component.EntityComponents
import net.minecraft.item.Item
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS

open class ItemWithComponents(properties: Properties) : Item(properties) {
	val components = EntityComponents()
	
	final override fun onItemUse(context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		return components.handle<UseOnBlockComponent, ActionResultType> { useOnBlock(context.world, context.pos, player, context.item, context) } ?: PASS
	}
}
