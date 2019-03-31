package chylex.hee.game.mechanics.instability.region.components
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.TileEntityEnergyCluster.LeakType
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.removeItem
import net.minecraft.entity.effect.EntityLightningBolt
import java.util.Random
import kotlin.math.pow

internal object ClusterLeakLogic{
	private val LEAK_GROUPS
		get() = intArrayOf(15, 25, 35, 45, 55, 65, 75, 85)
	
	fun call(clusters: List<TileEntityEnergyCluster>, rand: Random){
		if (clusters.isNotEmpty()){
			performLeaking(clusters, rand)
			performDeterioration(clusters, rand)
		}
	}
	
	private fun performLeaking(clusters: List<TileEntityEnergyCluster>, rand: Random){
		val remainingClusters = clusters.toMutableList()
		val assignedClusters = mutableListOf<Pair<TileEntityEnergyCluster, Int>>()
		
		do{
			val remainingGroups = LEAK_GROUPS.toMutableList().apply { shuffle(rand) }
			
			while(remainingClusters.isNotEmpty() && remainingGroups.isNotEmpty()){
				val cluster = rand.removeItem(remainingClusters)
				val group = rand.removeItem(remainingGroups)
				
				assignedClusters.add(cluster to group)
			}
		}while(remainingClusters.isNotEmpty())
		
		for((cluster, group) in assignedClusters){
			val percent = group + rand.nextInt(-5, 4)
			cluster.leakEnergy(cluster.energyLevel.units * (percent * 0.01F), LeakType.INSTABILITY)
		}
	}
	
	private fun performDeterioration(clusters: List<TileEntityEnergyCluster>, rand: Random){
		val canDeteriorate = clusters.mapNotNull {
			val healthFactor = when(it.currentHealth){
				HEALTHY  -> 13
				WEAKENED ->  4
				TIRED    ->  1
				else     -> return@mapNotNull null
			}
			
			val units = it.energyLevel.units.value.toFloat()
			val weight = (10 + units.pow(0.8F).ceilToInt()) * healthFactor
			
			return@mapNotNull weight to it
		}
		
		if (canDeteriorate.isNotEmpty()){
			val toDeteriorate = WeightedList(canDeteriorate).generateItem(rand)
			
			if (toDeteriorate.deteriorateHealth()){
				val world = toDeteriorate.world
				
				val lightningPos = toDeteriorate.pos.add(rand.nextInt(-24, 24), 0, rand.nextInt(-24, 24))
				val lightningY = world.getHeight(lightningPos.x, lightningPos.z)
				
				world.addWeatherEffect(EntityLightningBolt(world, lightningPos.x + 0.5, lightningY.toDouble(), lightningPos.z + 0.5, true))
			}
		}
	}
}
