package chylex.hee.game.world.structure.world
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.TransformedStructureTrigger
import chylex.hee.game.world.util.Size
import chylex.hee.game.world.util.Transform
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class TransformedStructureWorld(private val wrapped: IStructureWorld, private val size: Size, private val transform: Transform) : IStructureWorld{
	override val rand = wrapped.rand
	
	private val reverseTransform = transform.reverse
	
	override fun getState(pos: BlockPos): BlockState{
		return reverseTransform(wrapped.getState(transform(pos, size)))
	}
	
	override fun setState(pos: BlockPos, state: BlockState){
		wrapped.setState(transform(pos, size), transform(state))
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		wrapped.addTrigger(transform(pos, size), TransformedStructureTrigger(trigger, transform))
	}
	
	override fun finalize(){
		wrapped.finalize()
	}
}
