package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IBlockCollideWithEntityComponent {
	fun collide(state: BlockState, world: World, pos: BlockPos, entity: Entity)
}
