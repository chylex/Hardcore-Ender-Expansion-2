package chylex.hee.game.world.structure
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

interface IStructureTrigger{
	fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform)
	fun realize(world: IWorld, pos: BlockPos, transform: Transform)
	
	// Ugly implementation compatibility details...
	
	@JvmDefault
	val wrappedInstance: IStructureTrigger
		get() = this
	
	@JvmDefault
	fun rewrapInstance(trigger: IStructureTrigger): IStructureTrigger{
		return trigger
	}
}
