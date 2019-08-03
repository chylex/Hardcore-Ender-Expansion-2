package chylex.hee.game.item
import chylex.hee.game.block.BlockFlowerPotCustom
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.tileentity.TileEntityFlowerPot
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemBlockPlant(block: Block, private val potted: BlockFlowerPotCustom) : ItemBlockWithMetadata(block){
	private fun checkEmptyFlowerPot(world: World, pos: BlockPos): Boolean{
		return pos.getTile<TileEntityFlowerPot>(world)?.flowerItemStack?.isEmpty == true
	}
	
	override fun canPlaceBlockOnSide(world: World, pos: BlockPos, side: EnumFacing, player: EntityPlayer, stack: ItemStack): Boolean{
		return super.canPlaceBlockOnSide(world, pos, side, player, stack) || checkEmptyFlowerPot(world, pos)
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		if (checkEmptyFlowerPot(world, pos)){
			val heldItem = player.getHeldItem(hand)
			
			pos.setState(world, potted.getStateFromMeta(heldItem.metadata))
			player.addStat(StatList.FLOWER_POTTED)
			
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ)
	}
}
