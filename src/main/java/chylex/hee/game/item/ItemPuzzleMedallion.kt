package chylex.hee.game.item
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.util.getTile
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemPuzzleMedallion : Item(){
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val heldItem = player.getHeldItem(hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		val tile = pos.getTile<TileEntityMinersBurialAltar>(world)
		
		if (tile != null && !tile.hasMedallion){
			tile.hasMedallion = true
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
}
