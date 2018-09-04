package chylex.hee.game.block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockEndium(builder: BlockSimple.Builder): BlockSimple(builder){
	override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer): Boolean{
		return super.canHarvestBlock(world, pos, player) || player.getHeldItem(MAIN_HAND).item == Items.GOLDEN_PICKAXE
	}
}
