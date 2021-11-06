package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.World

interface IBlockExplodedComponent {
	fun canDrop(explosion: Explosion): Boolean
	fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion)
}
