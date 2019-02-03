package chylex.hee.game.world.structure.world
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

class OffsetStructureWorld(private val wrapped: IStructureWorld, private val offset: BlockPos) : IStructureWorld{
	override val rand = wrapped.rand
	
	override fun getState(pos: BlockPos): IBlockState{
		return wrapped.getState(pos.add(offset))
	}
	
	override fun setState(pos: BlockPos, state: IBlockState){
		wrapped.setState(pos.add(offset), state)
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		wrapped.addTrigger(pos.add(offset), trigger)
	}
	
	override fun finalize(){
		wrapped.finalize()
	}
}
