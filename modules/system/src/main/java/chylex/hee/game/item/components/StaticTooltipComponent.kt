package chylex.hee.game.item.components

import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

class StaticTooltipComponent(private vararg val lines: ITextComponent) : ITooltipComponent {
	override fun add(lines: MutableList<ITextComponent>, stack: ItemStack, advanced: Boolean, world: World?) {
		lines.addAll(this.lines)
	}
}
