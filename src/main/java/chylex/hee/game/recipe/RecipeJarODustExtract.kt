package chylex.hee.game.recipe

import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.inventory.util.size
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.world.World

object RecipeJarODustExtract : RecipeBaseDynamic() {
	override fun canFit(width: Int, height: Int): Boolean {
		return (width * height) >= 1
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean {
		val iterator = inv.nonEmptySlots.takeIf { it.hasNext() } ?: return false
		val first = iterator.next()
		
		return !iterator.hasNext() && getLayers(first.stack)?.contents?.isNotEmpty() == true
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack {
		val first = inv.nonEmptySlots.next()
		return getLayers(first.stack)?.getDust(BOTTOM) ?: ItemStack.EMPTY
	}
	
	override fun getRemainingItems(inv: CraftingInventory): NonNullList<ItemStack> {
		return NonNullList.withSize(inv.size, ItemStack.EMPTY).also {
			val first = inv.nonEmptySlots.next()
			val layers = getLayers(first.stack)
			
			if (layers != null) {
				layers.removeDust(BOTTOM)
				it[first.slot] = first.stack.copy().also { stack -> BlockJarODust.setLayersInStack(stack, layers) }
			}
		}
	}
	
	private fun getLayers(stack: ItemStack) = BlockJarODust.getLayersFromStack(stack)
}
