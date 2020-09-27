package chylex.hee.game.block
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import net.minecraft.block.BlockState
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockFallingObsidian(builder: BlockBuilder) : BlockSimple(builder){
	override fun tickRate(world: IWorldReader): Int{
		return 2
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		world.pendingBlockTicks.scheduleTick(pos, this, tickRate(world))
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		world.pendingBlockTicks.scheduleTick(pos, this, tickRate(world))
		return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		if (world.isRemote){
			return
		}
		
		if (EntityFallingBlockHeavy.canFallThrough(world, pos.down()) && pos.y >= 0){
			world.addEntity(EntityFallingObsidian(world, pos, defaultState))
		}
	}
}
