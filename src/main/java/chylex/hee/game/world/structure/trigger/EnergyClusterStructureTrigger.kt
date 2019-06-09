package chylex.hee.game.world.structure.trigger
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EnergyClusterStructureTrigger(private val snapshot: ClusterSnapshot) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		pos.setBlock(world, ModBlocks.ENERGY_CLUSTER)
		pos.getTile<TileEntityEnergyCluster>(world)?.loadClusterSnapshot(snapshot)
	}
}
