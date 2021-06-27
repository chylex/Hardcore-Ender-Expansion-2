package chylex.hee.game.world.structure

import chylex.hee.game.world.math.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

interface IStructureTrigger {
	fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform)
	fun realize(world: IServerWorld, pos: BlockPos, transform: Transform)
	
	// Ugly implementation compatibility details...
	
	val wrappedInstance: IStructureTrigger
		get() = this
	
	fun rewrapInstance(trigger: IStructureTrigger): IStructureTrigger {
		return trigger
	}
}
