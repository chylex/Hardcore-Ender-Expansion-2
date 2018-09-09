package chylex.hee.game.item.util
import chylex.hee.system.util.getBlock
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos

object BlockEditor{
	
	// Utilities
	
	fun canEdit(pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
		return player.canPlayerEdit(pos.offset(UP), UP, stack)
	}
	
	// Placement
	
	fun place(blockState: IBlockState, player: EntityPlayer, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing): BlockPos?{
		val block = blockState.block
		val world = player.world
		
		val targetPos = if (clickedPos.getBlock(world).isReplaceable(world, clickedPos))
			clickedPos
		else
			clickedPos.offset(clickedFacing)
		
		if (!player.canPlayerEdit(targetPos, clickedFacing, stack) || !world.mayPlace(block, targetPos, false, clickedFacing, null)){
			return null
		}
		
		if (!ItemBlock(block).placeBlockAt(stack, player, world, targetPos, clickedFacing, 0.5F, 0.5F, 0.5F, blockState)){
			return null
		}
		
		val sound = block.getSoundType(blockState, world, targetPos, player)
		world.playSound(player, targetPos, sound.placeSound, SoundCategory.BLOCKS, (sound.volume + 1F) / 2F, sound.pitch * 0.8F)
		
		return targetPos
	}
	
	fun place(block: Block, player: EntityPlayer, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing): BlockPos?{
		return place(block.defaultState, player, stack, clickedPos, clickedFacing)
	}
}
