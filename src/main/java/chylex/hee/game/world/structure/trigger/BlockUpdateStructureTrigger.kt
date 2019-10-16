package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockUpdateStructureTrigger(private val state: IBlockState) : IStructureTrigger{
	constructor(block: Block) : this(block.defaultState)
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		world.setState(pos, transform(state))
	}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		world.immediateBlockTick(pos, transform(state), world.rand)
		world.neighborChanged(pos, state.block, pos) // needed for liquids
	}
}
