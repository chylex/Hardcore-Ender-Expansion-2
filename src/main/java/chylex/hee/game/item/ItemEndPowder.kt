package chylex.hee.game.item
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.util.getBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.PASS
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemEndPowder : Item(){
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val heldItem = player.getHeldItem(hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		val block = pos.getBlock(world)
		
		if (block is IBlockDeathFlowerDecaying){
			if (!world.isRemote){
				block.healDeathFlower(world, pos)
			}
			
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean{
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity{
		return EntityItemCauldronTrigger(world, stack, replacee)
	}
}
