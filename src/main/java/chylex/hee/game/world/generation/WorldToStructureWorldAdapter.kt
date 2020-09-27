package chylex.hee.game.world.generation
import chylex.hee.game.block.BlockGloomtorch
import chylex.hee.game.world.FLAG_REPLACE_NO_DROPS
import chylex.hee.game.world.FLAG_SYNC_CLIENT
import chylex.hee.game.world.getState
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.setState
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.FluidStructureTrigger
import chylex.hee.system.migration.BlockButton
import chylex.hee.system.migration.BlockFlowingFluid
import chylex.hee.system.migration.BlockRedstoneWire
import chylex.hee.system.migration.BlockTorch
import chylex.hee.system.migration.BlockVine
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class WorldToStructureWorldAdapter(private val world: World, override val rand: Random, private val offset: BlockPos) : IStructureWorld{
	private fun requiresSecondPass(block: Block) = when(block){
		is BlockVine,
		is BlockTorch,
		is BlockGloomtorch,
		is BlockButton,
		is BlockRedstoneWire -> true
		
		else -> false
	}
	
	private val secondPass = mutableMapOf<BlockPos, BlockState>()
	
	override fun getState(pos: BlockPos): BlockState{
		return secondPass[pos] ?: pos.add(offset).getState(world)
	}
	
	override fun setState(pos: BlockPos, state: BlockState){
		val block = state.block
		
		if (requiresSecondPass(block)){
			secondPass[pos] = state
			return
		}
		else{
			secondPass.remove(pos) // avoids attempting to generate 2 blocks in one position if one requires second pass
		}
		
		val worldPos = pos.add(offset)
		worldPos.setState(world, state, FLAG_SYNC_CLIENT or FLAG_REPLACE_NO_DROPS)
		
		if (block is BlockFlowingFluid){
			FluidStructureTrigger(block).realize(world, worldPos, Transform.NONE)
		}
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		trigger.setup(this, pos, Transform.NONE)
		trigger.realize(world, pos.add(offset), Transform.NONE)
	}
	
	override fun finalize(){
		for((pos, state) in secondPass){
			pos.add(offset).setState(world, state, FLAG_SYNC_CLIENT or FLAG_REPLACE_NO_DROPS)
		}
		
		secondPass.clear()
	}
}
