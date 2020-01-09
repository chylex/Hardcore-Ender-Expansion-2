package chylex.hee.game.block
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import java.util.Random

class BlockDeathFlowerDecaying(builder: BlockBuilder) : BlockEndPlant(builder), IBlockDeathFlowerDecaying{
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(LEVEL)
	}
	
	override fun getItem(world: IBlockReader, pos: BlockPos, state: BlockState): ItemStack{
		return ItemStack(this).also { ItemDeathFlower.setDeathLevel(it, state[LEVEL]) }
	}
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: IWorldReader): Int{
		return implTickRate()
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState{
		return defaultState.with(LEVEL, ItemDeathFlower.getDeathLevel(context.item))
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
