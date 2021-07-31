package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

open class BlockDustyStoneBricks(builder: BlockBuilder) : BlockDustyStone(builder) {
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean {
		return isPickaxeOrShovel(player, player.getHeldItem(MAIN_HAND))
	}
	
	class Cracked(builder: BlockBuilder) : BlockDustyStoneBricks(builder) {
		override val localization
			get() = LocalizationStrategy.MoveToBeginning(wordCount = 1, wordOffset = 1)
	}
}
