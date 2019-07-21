package chylex.hee.game.mechanics.table
import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.entity.TileEntityBaseTable
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.world.util.RayTracer
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.center
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setPos
import chylex.hee.system.util.square
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable

class TableEnergyClusterHandler(private val table: TileEntityBaseTable, maxDistance: Int) : INBTSerializable<NBTTagCompound>{
	private val maxDistanceSq = square(maxDistance)
	private val rayTracer = RayTracer(::canCollideCheck)
	
	private var currentCluster: BlockPos? by table.MarkDirtyOnChange(null)
	
	fun drainEnergy(amount: IEnergyQuantity): Boolean{
		val cluster = currentCluster?.getTile<TileEntityEnergyCluster>(table.world)?.takeIf(::checkLineOfSight) ?: findNewCluster()
		
		if (cluster == null || !cluster.drainEnergy(amount)){
			currentCluster = null
			return false
		}
		
		currentCluster = cluster.pos
		table.particleHandler.onClusterDrained(cluster.pos)
		return true
	}
	
	// Behavior
	
	private fun canCollideCheck(@Suppress("UNUSED_PARAMETER") world: World, pos: BlockPos, state: IBlockState): Boolean{
		val block = state.block
		
		return if (block is BlockAbstractTableTile<*>)
			pos != table.pos
		else
			block !== ModBlocks.ENERGY_CLUSTER && block.canCollideCheck(state, false)
	}
	
	private fun checkLineOfSight(cluster: TileEntityEnergyCluster): Boolean{
		return rayTracer.traceBlocksBetweenVectors(table.world, table.pos.center, cluster.pos.center)?.typeOfHit != BLOCK
	}
	
	private fun isValidCandidate(cluster: TileEntityEnergyCluster): Boolean{
		return (
			cluster.pos.distanceSqTo(table.pos) <= maxDistanceSq &&
			cluster.currentHealth.regenSpeedMp > 0F &&
			(cluster.energyLevel.floating.value / cluster.energyBaseCapacity.floating.value) >= 0.1F &&
			checkLineOfSight(cluster)
		)
	}
	
	private fun pickBetterCandidate(best: TileEntityEnergyCluster, potential: TileEntityEnergyCluster): TileEntityEnergyCluster{
		if (!potential.wasUsedRecently && best.wasUsedRecently){
			return potential
		}
		
		if (potential.currentHealth.regenSpeedMp > best.currentHealth.regenSpeedMp){
			return potential
		}
		
		if (potential.energyLevel > best.energyLevel){
			return potential
		}
		
		return best
	}
	
	private fun findNewCluster(): TileEntityEnergyCluster?{
		val candidates = ArrayList<TileEntityEnergyCluster>(4)
		
		for(tile in table.world.tickableTileEntities){
			if (tile is TileEntityEnergyCluster && isValidCandidate(tile)){
				candidates.add(tile)
			}
		}
		
		return candidates.takeIf { it.isNotEmpty() }?.reduce(::pickBetterCandidate)
	}
	
	// Serialization
	
	override fun serializeNBT() = NBTTagCompound().apply {
		currentCluster?.let {
			setPos("Pos", it)
		}
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		currentCluster = getPosOrNull("Pos")
	}
}
