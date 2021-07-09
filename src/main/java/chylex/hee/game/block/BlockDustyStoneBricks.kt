package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockDustyStoneBricks(builder: BlockBuilder) : BlockDustyStone(builder) {
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: EntityPlayer): Boolean {
		return isPickaxeOrShovel(player, player.getHeldItem(MAIN_HAND))
	}
}
