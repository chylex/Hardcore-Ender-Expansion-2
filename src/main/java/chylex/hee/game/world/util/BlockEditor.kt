package chylex.hee.game.world.util
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
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

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
	
	private fun placeInternal(state: IBlockState, player: EntityPlayer, stack: ItemStack, targetPos: BlockPos, clickedFacing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): BlockPos?{
		val block = state.block
		val world = player.world
		
		if (!player.canPlayerEdit(targetPos, clickedFacing, stack) || !world.mayPlace(block, targetPos, false, clickedFacing, null)){
			return null
		}
		
		if (!ItemBlock(block).placeBlockAt(stack, player, world, targetPos, clickedFacing, hitX, hitY, hitZ, state)){
			return null
		}
		
		val sound = block.getSoundType(state, world, targetPos, player)
		sound.placeSound.playUniversal(player, targetPos, SoundCategory.BLOCKS, volume = (sound.volume + 1F) / 2F, pitch = sound.pitch * 0.8F)
		
		return targetPos
	}
	
	private fun getTargetPos(world: World, clickedPos: BlockPos, clickedFacing: EnumFacing): BlockPos{
		return if (clickedPos.isReplaceable(world))
			clickedPos
		else
			clickedPos.offset(clickedFacing)
	}
	
	fun place(state: IBlockState, player: EntityPlayer, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): BlockPos?{
		return placeInternal(state, player, stack, getTargetPos(player.world, clickedPos, clickedFacing), clickedFacing, hitX, hitY, hitZ)
	}
	
	fun place(block: Block, player: EntityPlayer, hand: EnumHand, stack: ItemStack, clickedPos: BlockPos, clickedFacing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): BlockPos?{
		val world = player.world
		val targetPos = getTargetPos(world, clickedPos, clickedFacing)
		val placedState = block.getStateForPlacement(world, targetPos, clickedFacing, hitX, hitY, hitZ, stack.metadata, player, hand)
		
		return placeInternal(placedState, player, stack, targetPos, clickedFacing, hitX, hitY, hitZ)
	}
}
