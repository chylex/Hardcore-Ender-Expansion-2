package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockDustyStoneBricks(builder: BlockBuilder) : BlockDustyStone(builder){
	override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer): Boolean{
		return isPickaxeOrShovel(player.getHeldItem(MAIN_HAND))
	}
}
