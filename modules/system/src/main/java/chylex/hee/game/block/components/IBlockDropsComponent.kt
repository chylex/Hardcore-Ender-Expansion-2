package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

interface IBlockDropsComponent {
	fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>
	
	fun getPickBlock(state: BlockState, world: IBlockReader, pos: BlockPos): ItemStack? {
		return null
	}
}
