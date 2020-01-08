package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockEndium(builder: BlockBuilder): BlockSimple(builder){
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean{
		return super.canHarvestBlock(state, world, pos, player) || player.getHeldItem(MAIN_HAND).item === Items.GOLDEN_PICKAXE
	}
}
