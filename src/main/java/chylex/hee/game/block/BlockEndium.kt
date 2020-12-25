package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockEndium(builder: BlockBuilder) : BlockSimple(builder) {
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: EntityPlayer): Boolean {
		return super.canHarvestBlock(state, world, pos, player) || player.getHeldItem(MAIN_HAND).item === Items.GOLDEN_PICKAXE
	}
}
