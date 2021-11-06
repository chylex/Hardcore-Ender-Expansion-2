package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IPlayerUseBlockComponent {
	fun use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand): ActionResultType
}
