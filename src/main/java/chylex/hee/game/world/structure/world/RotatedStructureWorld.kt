package chylex.hee.game.world.structure.world
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.RotatedStructureTrigger
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import net.minecraft.block.state.IBlockState
import net.minecraft.util.Rotation
import net.minecraft.util.Rotation.CLOCKWISE_180
import net.minecraft.util.Rotation.CLOCKWISE_90
import net.minecraft.util.Rotation.COUNTERCLOCKWISE_90
import net.minecraft.util.Rotation.NONE
import net.minecraft.util.math.BlockPos

class RotatedStructureWorld(private val wrapped: IStructureWorld, private val size: Size, private val rotation: Rotation) : IStructureWorld{
	companion object{
		fun rotatePos(pos: BlockPos, size: Size, rotation: Rotation): BlockPos{
			val (x, y, z) = pos
			
			val rotatedX: Int
			val rotatedZ: Int
			
			when(rotation){
				NONE ->
				{ rotatedX = x; rotatedZ = z }
				CLOCKWISE_90 ->
				{ rotatedX = size.maxZ - z; rotatedZ = x }
				CLOCKWISE_180 ->
				{ rotatedX = size.maxX - x; rotatedZ = size.maxZ - z }
				COUNTERCLOCKWISE_90 ->
				{ rotatedX = z; rotatedZ = size.maxX - x }
			}
			
			return Pos(rotatedX, y, rotatedZ)
		}
	}
	
	override val rand = wrapped.rand
	
	private val reverseRotation = when(rotation){
		NONE -> NONE
		CLOCKWISE_90 -> COUNTERCLOCKWISE_90
		CLOCKWISE_180 -> CLOCKWISE_180
		COUNTERCLOCKWISE_90 -> CLOCKWISE_90
	}
	
	override fun getState(pos: BlockPos): IBlockState{
		return wrapped.getState(rotatePos(pos, size, rotation)).withRotation(reverseRotation)
	}
	
	override fun setState(pos: BlockPos, state: IBlockState){
		wrapped.setState(rotatePos(pos, size, rotation), state.withRotation(rotation))
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		wrapped.addTrigger(rotatePos(pos, size, rotation), RotatedStructureTrigger(trigger, rotation))
	}
}
