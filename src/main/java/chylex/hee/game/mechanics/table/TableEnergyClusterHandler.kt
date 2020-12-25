package chylex.hee.game.mechanics.table

import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.world.center
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.getTile
import chylex.hee.system.math.square
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.util.INBTSerializable

class TableEnergyClusterHandler(private val table: TileEntityBaseTable, maxDistance: Int) : INBTSerializable<TagCompound> {
	private companion object {
		private const val POS_TAG = "Pos"
	}
	
	private val maxDistanceSq = square(maxDistance)
	private var currentCluster: BlockPos? by table.MarkDirtyOnChange(null)
	
	fun drainEnergy(amount: IEnergyQuantity): Boolean {
		val cluster = currentCluster?.getTile<TileEntityEnergyCluster>(table.wrld)?.takeIf(::checkLineOfSight) ?: findNewCluster()
		
		if (cluster == null || !cluster.drainEnergy(amount)) {
			currentCluster = null
			return false
		}
		
		currentCluster = cluster.pos
		table.particleHandler.onClusterDrained(cluster.pos)
		return true
	}
	
	// Behavior
	
	private inner class RayTraceObstacles(startVec: Vec3d, endVec: Vec3d) : RayTraceContext(startVec, endVec, BlockMode.COLLIDER, FluidMode.NONE, null) {
		override fun getBlockShape(state: BlockState, world: IBlockReader, pos: BlockPos): VoxelShape {
			val block = state.block
			
			return if (block is BlockAbstractTableTile<*> && pos == table.pos)
				VoxelShapes.empty()
			else
				BlockMode.COLLIDER.get(state, world, pos, ISelectionContext.dummy())
		}
	}
	
	private fun checkLineOfSight(cluster: TileEntityEnergyCluster): Boolean {
		return table.wrld.rayTraceBlocks(RayTraceObstacles(table.pos.center, cluster.pos.center)).type == Type.MISS
	}
	
	private fun isValidCandidate(cluster: TileEntityEnergyCluster): Boolean {
		return (
			cluster.pos.distanceSqTo(table.pos) <= maxDistanceSq &&
			cluster.currentHealth.regenSpeedMp > 0F &&
			(cluster.energyLevel.floating.value / cluster.energyBaseCapacity.floating.value) >= 0.1F &&
			checkLineOfSight(cluster)
		)
	}
	
	private fun pickBetterCandidate(best: TileEntityEnergyCluster, potential: TileEntityEnergyCluster): TileEntityEnergyCluster {
		if (!potential.wasUsedRecently && best.wasUsedRecently) {
			return potential
		}
		
		if (potential.currentHealth.regenSpeedMp > best.currentHealth.regenSpeedMp) {
			return potential
		}
		
		if (potential.energyLevel > best.energyLevel) {
			return potential
		}
		
		return best
	}
	
	private fun findNewCluster(): TileEntityEnergyCluster? {
		val candidates = ArrayList<TileEntityEnergyCluster>(4)
		
		for(tile in table.wrld.tickableTileEntities) {
			if (tile is TileEntityEnergyCluster && isValidCandidate(tile)) {
				candidates.add(tile)
			}
		}
		
		return candidates.takeIf { it.isNotEmpty() }?.reduce(::pickBetterCandidate)
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		currentCluster?.let {
			putPos(POS_TAG, it)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		currentCluster = getPosOrNull(POS_TAG)
	}
}
