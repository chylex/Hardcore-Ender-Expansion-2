package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.Tags
import java.util.Random

class BlockFallingObsidian(builder: BlockBuilder) : HeeBlock(builder) {
	override val localization
		get() = LocalizationStrategy.MoveToBeginning(wordCount = 1)
	
	override val model
		get() = BlockStateModels.Cube(Blocks.OBSIDIAN)
	
	override val drop
		get() = BlockDrop.OneOf(Blocks.OBSIDIAN)
	
	override val tags
		get() = listOf(Tags.Blocks.OBSIDIAN)
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		world.pendingBlockTicks.scheduleTick(pos, this, 2)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		world.pendingBlockTicks.scheduleTick(pos, this, 2)
		@Suppress("DEPRECATION")
		return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		if (world.isRemote) {
			return
		}
		
		if (EntityFallingBlockHeavy.canFallThrough(world, pos.down()) && pos.y >= 0) {
			world.addEntity(EntityFallingObsidian(world, pos, defaultState))
		}
	}
}
