package chylex.hee.game.world.generation.trigger

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class EnergyClusterStructureTrigger(private val snapshot: ClusterSnapshot) : IStructureTrigger {
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		world.setBlock(pos, ModBlocks.ENERGY_CLUSTER)
	}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
		TileEntityStructureTrigger.addTileSafe(world, pos, TileEntityEnergyCluster().apply { loadClusterSnapshot(snapshot, inactive = true) })
	}
}
