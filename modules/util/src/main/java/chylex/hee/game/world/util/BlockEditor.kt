package chylex.hee.game.world.util

import chylex.hee.game.fx.util.playUniversal
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext

object BlockEditor {
	
	// Utilities
	
	fun canEdit(pos: BlockPos, player: PlayerEntity, stack: ItemStack): Boolean {
		return player.canPlayerEdit(pos.offset(UP), UP, stack)
	}
	
	fun canBreak(pos: BlockPos, player: PlayerEntity): Boolean {
		val world = player.world
		return (pos.isAir(world) || pos.getHardness(world) >= 0F) && player.abilities.allowEdit
	}
	
	// Placement
	
	private fun placeInternal(state: BlockState, player: PlayerEntity, stack: ItemStack, targetPos: BlockPos, clickedFacing: Direction): BlockPos? {
		val block = state.block
		val world = player.world
		
		if (!player.canPlayerEdit(targetPos, clickedFacing, stack) ||
		    !state.isValidPosition(world, targetPos) ||
		    !world.placedBlockCollides(state, targetPos, ISelectionContext.dummy())
		) {
			return null
		}
		
		targetPos.setState(world, state)
		
		if (player is ServerPlayerEntity) {
			CriteriaTriggers.PLACED_BLOCK.trigger(player, targetPos, stack)
		}
		
		block.getSoundType(state, world, targetPos, player).let {
			it.placeSound.playUniversal(player, targetPos, SoundCategory.BLOCKS, volume = (it.volume + 1F) / 2F, pitch = it.pitch * 0.8F)
		}
		
		return targetPos
	}
	
	fun place(block: Block, player: PlayerEntity, stack: ItemStack, context: ItemUseContext): BlockPos? {
		val world = context.world
		val blockContext = BlockItemUseContext(context)
		
		if (context.pos.getState(world).isReplaceable(blockContext)) {
			blockContext.replacingClickedOnBlock()
		}
		
		return block.getStateForPlacement(blockContext)?.let { placeInternal(it, player, stack, blockContext.pos, context.face) }
	}
}
