package chylex.hee.game.mechanics.energy
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.util.nextFloat
import java.util.Random

interface IClusterGenerator{
	fun generate(rand: Random): ClusterSnapshot
	
	companion object{ // TODO make generators static fields in kotlin 1.3
		private class SimpleGenerator(level: Pair<Int, Int>, capacity: Pair<Int, Int>, private val health: (Random) -> HealthStatus) : IClusterGenerator{
			constructor(level: Pair<Int, Int>, capacity: Pair<Int, Int>, health: WeightedList<HealthStatus>) : this(level, capacity, health::generateItem)
			
			private val levelMin = Units(level.first).floating.value
			private val levelMax = Units(level.second).floating.value
			
			private val capacityMin = Units(capacity.first).floating.value
			private val capacityMax = Units(capacity.second).floating.value
			
			override fun generate(rand: Random): ClusterSnapshot{
				val generatedLevel = Floating(rand.nextFloat(levelMin, levelMax))
				val generatedCapacity = Floating(rand.nextFloat(capacityMin, capacityMax))
				
				return ClusterSnapshot(
					energyLevel    = minOf(generatedLevel, generatedCapacity),
					energyCapacity = generatedCapacity,
					healthStatus   = health(rand),
					healthOverride = null,
					color          = ClusterColor.generate(rand)
				)
			}
		}
	}
}
