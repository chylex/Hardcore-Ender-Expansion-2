package chylex.hee.game.block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockDustyStoneBricks(builder: BlockSimple.Builder) : BlockDustyStone(builder){
	override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer): Boolean{
		return isPickaxeOrShovel(player.getHeldItem(MAIN_HAND))
	}
}
