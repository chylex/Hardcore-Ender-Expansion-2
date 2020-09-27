package chylex.hee.game.mechanics.table
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.particle.ParticleEnergyTableDrain
import chylex.hee.game.particle.ParticleEnergyTransferToPedestal
import chylex.hee.game.particle.base.ParticleBaseEnergy.ClusterParticleDataGenerator
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.InSphere
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.center
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getTile
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.IFxData
import chylex.hee.network.fx.IFxHandler
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.floorToInt
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.readPos
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writePos
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class TableParticleHandler(private val table: TileEntityBaseTable){
	companion object{
		private fun getParticleRate(processTickRate: Int): Int{
			val defaultRate = processTickRate * 2
			
			return if (defaultRate < 20)
				10 + ((defaultRate - 1F) * 0.5F).floorToInt()
			else
				defaultRate
		}
		
		private val PARTICLE_PEDESTAL_POS = InBox(0.02F)
		
		private val PARTICLE_CLUSTER_POS = InSphere(0.05F)
		private val PARTICLE_CLUSTER_MOT = InBox(0.004F)
		
		class FxProcessPedestalsData(private val table: TileEntityBaseTable, private val targetPositions: List<BlockPos>, private val travelTime: Int) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(table.pos)
				writeByte(travelTime)
				writeByte(targetPositions.size)
				
				for(pos in targetPositions){
					writeLong(pos.toLong())
				}
			}
		}
		
		val FX_PROCESS_PEDESTALS = object : IFxHandler<FxProcessPedestalsData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val table = readPos().getTile<TileEntityBaseTable>(world) ?: return
				val travelTime = readByte().toInt()
				
				repeat(readByte().toInt()){
					val targetPos = readPos()
					
					if (targetPos.getBlock(world) === ModBlocks.TABLE_PEDESTAL){
						ParticleSpawnerCustom(
							type = ParticleEnergyTransferToPedestal,
							data = ParticleEnergyTransferToPedestal.Data(targetPos, travelTime),
							pos = PARTICLE_PEDESTAL_POS,
							maxRange = 64.0
						).spawn(Point(table.pos, 1), rand)
					}
				}
			}
		}
		
		class FxDrainClusterData(private val table: TileEntityBaseTable, private val clusterPos: BlockPos) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(clusterPos)
				writePos(table.pos)
			}
		}
		
		val FX_DRAIN_CLUSTER = object : IFxHandler<FxDrainClusterData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val cluster = readPos().getTile<TileEntityEnergyCluster>(world) ?: return
				val table = readPos().getTile<TileEntityBaseTable>(world) ?: return
				
				val clusterPos = cluster.pos.center
				val tablePos = table.pos.center
				
				val dirVec = clusterPos.directionTowards(tablePos)
				val startPoint = clusterPos.add(dirVec.scale(rand.nextFloat(0.05, 0.37)))
				val endPoint = tablePos.subtract(dirVec.scale(0.27))
				
				ParticleSpawnerCustom(
					type = ParticleEnergyTableDrain,
					data = ClusterParticleDataGenerator(cluster),
					pos = PARTICLE_CLUSTER_POS,
					mot = PARTICLE_CLUSTER_MOT,
					maxRange = 64.0
				).spawn(Line(startPoint, endPoint, 0.33), rand)
			}
		}
	}
	
	private var lastUpdatedPedestals = mutableListOf<BlockPos>()
	private var lastDrainedCluster: BlockPos? = null
	
	fun tick(processTickRate: Int){
		val particleRate = getParticleRate(processTickRate)
		val modTick = table.wrld.totalTime % particleRate
		
		if (modTick == 0L){
			lastDrainedCluster?.let {
				PacketClientFX(FX_DRAIN_CLUSTER, FxDrainClusterData(table, it)).sendToAllAround(table, 76.0)
				lastDrainedCluster = null
			}
		}
		else if (modTick == 3L){
			if (lastUpdatedPedestals.isNotEmpty()){
				PacketClientFX(FX_PROCESS_PEDESTALS, FxProcessPedestalsData(table, lastUpdatedPedestals, (particleRate - 2).coerceAtMost(30))).sendToAllAround(table, 70.0)
				lastUpdatedPedestals.clear()
			}
		}
	}
	
	fun onPedestalsTicked(pedestals: Array<BlockPos>){
		this.lastUpdatedPedestals.addAll(pedestals)
	}
	
	fun onClusterDrained(cluster: BlockPos){
		this.lastDrainedCluster = cluster
	}
}
