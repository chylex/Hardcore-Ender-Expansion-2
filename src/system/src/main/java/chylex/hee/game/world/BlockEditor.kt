package chylex.hee.game.world
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityPlayerMP
import chylex.hee.system.migration.Facing.UP
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.Direction
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext

object BlockEditor{
	
	// Utilities
	
	fun canEdit(pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
		return player.canPlayerEdit(pos.offset(UP), UP, stack)
	}
	
	fun canBreak(pos: BlockPos, player: EntityPlayer): Boolean{
		val world = player.world
		return (pos.isAir(world) || pos.getHardness(world) >= 0F) && player.abilities.allowEdit
	}
	
	// Placement
	
	private fun placeInternal(state: BlockState, player: EntityPlayer, stack: ItemStack, targetPos: BlockPos, clickedFacing: Direction): BlockPos?{
		val block = state.block
		val world = player.world
		
		if (!player.canPlayerEdit(targetPos, clickedFacing, stack) ||
			!state.isValidPosition(world, targetPos) ||
			!world.func_226663_a_(state, targetPos, ISelectionContext.dummy()) // RENAME checks entity collisions
		){
			return null
		}
		
		targetPos.setState(world, state)
		
		if (player is EntityPlayerMP){
			CriteriaTriggers.PLACED_BLOCK.trigger(player, targetPos, stack)
		}
		
		block.getSoundType(state, world, targetPos, player).let {
			it.placeSound.playUniversal(player, targetPos, SoundCategory.BLOCKS, volume = (it.volume + 1F) / 2F, pitch = it.pitch * 0.8F)
		}
		
		return targetPos
	}
	
	fun place(block: Block, player: EntityPlayer, stack: ItemStack, context: ItemUseContext): BlockPos?{
		val world = context.world
		val blockContext = BlockItemUseContext(context)
		
		if (context.pos.getState(world).isReplaceable(blockContext)){
			blockContext.replacingClickedOnBlock()
		}
		
		return block.getStateForPlacement(blockContext)?.let { placeInternal(it, player, stack, blockContext.pos, context.face) }
	}
}
