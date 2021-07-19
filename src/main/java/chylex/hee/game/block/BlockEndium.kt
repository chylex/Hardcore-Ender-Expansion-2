package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockEndium(builder: BlockBuilder) : HeeBlock(builder) {
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean {
		return super.canHarvestBlock(state, world, pos, player) || player.getHeldItem(MAIN_HAND).item === Items.GOLDEN_PICKAXE
	}
}
