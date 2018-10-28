package chylex.hee.game.item.util
import chylex.hee.system.util.getHardness
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isReplaceable
import chylex.hee.system.util.playUniversal
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
	
	fun canBreak(pos: BlockPos, player: EntityPlayer): Boolean{
		val world = player.world
		return (pos.isAir(world) || pos.getHardness(world) >= 0F) && player.capabilities.allowEdit
	}
	
	// Placement
	
	fun place(blockState: IBlockState, player: EntityPlayer, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing): BlockPos?{
		val block = blockState.block
		val world = player.world
		
		val targetPos = if (clickedPos.isReplaceable(world))
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
		sound.placeSound.playUniversal(player, targetPos, SoundCategory.BLOCKS, volume = (sound.volume + 1F) / 2F, pitch = sound.pitch * 0.8F)
		
		return targetPos
	}
	
	fun place(block: Block, player: EntityPlayer, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing): BlockPos?{
		return place(block.defaultState, player, stack, clickedPos, clickedFacing)
	}
}
