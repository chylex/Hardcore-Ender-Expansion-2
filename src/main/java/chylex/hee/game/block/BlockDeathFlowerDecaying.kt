package chylex.hee.game.block
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import java.util.Random

class BlockDeathFlowerDecaying(builder: BlockBuilder) : BlockEndPlant(builder), IBlockDeathFlowerDecaying{
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(LEVEL)
	}
	
	// UPDATE override fun damageDropped(state: BlockState) = state[LEVEL] - MIN_LEVEL
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: IWorldReader): Int{
		return implTickRate()
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		super.onBlockAdded(state, world, pos, oldState, isMoving)
		implOnBlockAdded(world, pos)
	}
	
	override fun tick(state: BlockState, world: World, pos: BlockPos, rand: Random){
		super.tick(state, world, pos, rand)
		implUpdateTick(world, pos, state, rand)
	}
}
