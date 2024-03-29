package chylex.hee.game.item.components

import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

fun interface ITooltipComponent {
	fun add(lines: MutableList<ITextComponent>, stack: ItemStack, advanced: Boolean, world: World?)
}
