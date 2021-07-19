package chylex.hee.game.item.properties

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT, _interface = IItemColor::class)
abstract class ItemTint : IItemColor {
	protected companion object {
		const val NO_TINT = -1
	}
	
	@Sided(Side.CLIENT)
	final override fun getColor(stack: ItemStack, tintIndex: Int): Int {
		return tint(stack, tintIndex)
	}
	
	@Sided(Side.CLIENT)
	abstract fun tint(stack: ItemStack, tintIndex: Int): Int
}
