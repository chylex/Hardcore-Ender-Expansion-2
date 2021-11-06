package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

interface IBlockPlacementComponent {
	fun isPositionValid(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
		return true
	}
	
	fun getPlacedState(defaultState: BlockState, world: World, pos: BlockPos, context: BlockItemUseContext): BlockState {
		return defaultState
	}
	
	fun onPlacedBy(state: BlockState, world: World, pos: BlockPos, placer: LivingEntity?, stack: ItemStack) {}
}
