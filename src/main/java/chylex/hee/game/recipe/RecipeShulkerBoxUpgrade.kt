package chylex.hee.game.recipe
import chylex.hee.game.block.BlockShulkerBoxOverride.BoxSize
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.init.ModItems
import net.minecraft.block.Blocks
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.world.World

sealed class RecipeShulkerBoxUpgrade(private val fromSize: BoxSize, private val toSize: BoxSize, private val upgradeItem: Item) : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return width >= 3 && height >= 3
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean{
		return (
			getStackInRowAndColumn(inv, 1, 1).let { it.item is ItemShulkerBoxOverride && ItemShulkerBoxOverride.getBoxSize(it) == fromSize } &&
			
			getStackInRowAndColumn(inv, 0, 0).item === ModItems.ANCIENT_DUST &&
			getStackInRowAndColumn(inv, 0, 1).item === Items.SHULKER_SHELL &&
			getStackInRowAndColumn(inv, 0, 2).item === ModItems.ANCIENT_DUST &&
			
			getStackInRowAndColumn(inv, 1, 0).item === upgradeItem &&
			getStackInRowAndColumn(inv, 1, 2).item === upgradeItem &&
			
			getStackInRowAndColumn(inv, 2, 0).item === ModItems.ANCIENT_DUST &&
			getStackInRowAndColumn(inv, 2, 1).item === Items.SHULKER_SHELL &&
			getStackInRowAndColumn(inv, 2, 2).item === ModItems.ANCIENT_DUST
		)
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack{
		return getStackInRowAndColumn(inv, 1, 1).copy().also { ItemShulkerBoxOverride.setBoxSize(it, toSize) }
	}
	
	object SmallToMedium : RecipeShulkerBoxUpgrade(BoxSize.SMALL, BoxSize.MEDIUM, ModItems.AURICION)
	object MediumToLarge : RecipeShulkerBoxUpgrade(BoxSize.MEDIUM, BoxSize.LARGE, Blocks.BEDROCK.asItem()) // TODO update to hyperslot
}
