package chylex.hee.game.recipe
import chylex.hee.game.item.ItemVoidSalad.Type
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.getStack
import chylex.hee.system.util.nonEmptySlots
import com.google.common.collect.Iterators
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object RecipeVoidSalad : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return width == 3 && height >= 2
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean{
		val bowlRow = findBowlRow(inv)
		
		return (
			bowlRow != null &&
			getStackInRowAndColumn(inv, 0, bowlRow - 1).item.isFood &&
			getStackInRowAndColumn(inv, 1, bowlRow - 1).item === ModItems.VOID_ESSENCE &&
			getStackInRowAndColumn(inv, 2, bowlRow - 1).item.isFood &&
			Iterators.size(inv.nonEmptySlots) == 4
		)
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack{
		val bowlRow = findBowlRow(inv) ?: return ItemStack.EMPTY
		
		val isLeftVoidSalad = isSingleVoidSalad(getStackInRowAndColumn(inv, 0, bowlRow - 1))
		val isRightVoidSalad = isSingleVoidSalad(getStackInRowAndColumn(inv, 2, bowlRow - 1))
		
		return when{
			isLeftVoidSalad && isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD).apply { damage = Type.MEGA.ordinal }
			isLeftVoidSalad || isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD).apply { damage = Type.DOUBLE.ordinal }
			else                                -> ItemStack(ModItems.VOID_SALAD).apply { damage = Type.SINGLE.ordinal }
		}
	}
	
	private fun findBowlRow(inv: CraftingInventory): Int?{
		return (0 until inv.height).find { row -> getStackInRowAndColumn(inv, 1, row).item === Items.BOWL }
	}
	
	private fun isSingleVoidSalad(stack: ItemStack): Boolean{
		return stack.item === ModItems.VOID_SALAD && stack.damage == Type.SINGLE.ordinal
	}
	
	private fun getStackInRowAndColumn(inv: CraftingInventory, row: Int, column: Int): ItemStack{
		return inv.getStack(row + (column * inv.width))
	}
}
