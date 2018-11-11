package chylex.hee.game.recipe
import chylex.hee.game.item.ItemVoidSalad.Type
import chylex.hee.init.ModItems
import chylex.hee.system.util.nonEmptySlots
import com.google.common.collect.Iterators
import net.minecraft.init.Items
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object RecipeVoidSalad : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return width == 3 && height >= 2
	}
	
	override fun matches(inv: InventoryCrafting, world: World): Boolean{
		val bowlRow = findBowlRow(inv)
		
		return (
			bowlRow != null &&
			inv.getStackInRowAndColumn(0, bowlRow - 1).item is ItemFood &&
			inv.getStackInRowAndColumn(1, bowlRow - 1).item === ModItems.VOID_ESSENCE &&
			inv.getStackInRowAndColumn(2, bowlRow - 1).item is ItemFood &&
			Iterators.size(inv.nonEmptySlots) == 4
		)
	}
	
	override fun getCraftingResult(inv: InventoryCrafting): ItemStack{
		val bowlRow = findBowlRow(inv) ?: return ItemStack.EMPTY
		
		val isLeftVoidSalad = isSingleVoidSalad(inv.getStackInRowAndColumn(0, bowlRow - 1))
		val isRightVoidSalad = isSingleVoidSalad(inv.getStackInRowAndColumn(2, bowlRow - 1))
		
		return when{
			isLeftVoidSalad && isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD, 1, Type.MEGA.ordinal)
			isLeftVoidSalad || isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD, 1, Type.DOUBLE.ordinal)
			else                                -> ItemStack(ModItems.VOID_SALAD, 1, Type.SINGLE.ordinal)
		}
	}
	
	private fun findBowlRow(inv: InventoryCrafting): Int?{
		return (0 until inv.height).find { row -> inv.getStackInRowAndColumn(1, row).item === Items.BOWL }
	}
	
	private fun isSingleVoidSalad(stack: ItemStack): Boolean{
		return stack.item === ModItems.VOID_SALAD && stack.metadata == Type.SINGLE.ordinal
	}
}
