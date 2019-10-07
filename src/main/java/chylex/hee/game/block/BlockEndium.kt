package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockEndium(builder: BlockBuilder): BlockSimple(builder){
	override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer): Boolean{
		return super.canHarvestBlock(world, pos, player) || player.getHeldItem(MAIN_HAND).item === Items.GOLDEN_PICKAXE
	}
}
