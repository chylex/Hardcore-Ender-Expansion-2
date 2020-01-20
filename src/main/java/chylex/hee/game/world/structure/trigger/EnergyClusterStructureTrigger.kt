package chylex.hee.game.world.structure.trigger
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getTile
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class EnergyClusterStructureTrigger(private val snapshot: ClusterSnapshot) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		world.setBlock(pos, ModBlocks.ENERGY_CLUSTER)
	}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform){
		pos.getTile<TileEntityEnergyCluster>(world)?.loadClusterSnapshot(snapshot, inactive = true)
	}
}
