package chylex.hee.game.mechanics.energy
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.random.nextFloat
import java.util.Random

interface IClusterGenerator{
	fun generate(rand: Random): ClusterSnapshot
	
	companion object{
		private class SimpleGenerator(level: Pair<Int, Int>, capacity: Pair<Int, Int>, private val health: WeightedList<HealthStatus>) : IClusterGenerator{
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
					healthStatus   = health.generateItem(rand),
					healthOverride = null,
					color          = ClusterColor.generate(rand)
				)
			}
		}
		
		// Overworld
		
		@JvmField
		val OVERWORLD: IClusterGenerator = SimpleGenerator(
			level    = 0 to  7,
			capacity = 8 to 13,
			health   = weightedListOf(
				25 to HEALTHY,
				50 to WEAKENED,
				25 to TIRED
			)
		)
		
		@JvmField
		val STRONGHOLD: IClusterGenerator = SimpleGenerator(
			level    =  4 to 23,
			capacity = 18 to 35,
			health   = weightedListOf(
				 5 to HEALTHY,
				25 to WEAKENED,
				35 to TIRED,
				35 to DAMAGED
			)
		)
		
		@JvmField
		val ENERGY_SHRINE: IClusterGenerator = SimpleGenerator(
			level    =  60 to 150,
			capacity = 100 to 175,
			health   = weightedListOf(
				100 to HEALTHY
			)
		)
		
		@JvmStatic
		fun ARCANE_CONJUNCTIONS(rand: Random, amount: Int): Array<IClusterGenerator>{
			val level = 2 to 80
			
			val tiers = listOf(
				( 50 to 115) to weightedListOf(40 to HEALTHY, 45 to WEAKENED,              15 to UNSTABLE),
				(115 to 180) to weightedListOf(20 to HEALTHY, 55 to WEAKENED, 10 to TIRED, 15 to UNSTABLE),
				(180 to 245) to weightedListOf(10 to HEALTHY, 50 to WEAKENED, 25 to TIRED, 15 to UNSTABLE),
				(245 to 310) to weightedListOf( 5 to HEALTHY, 40 to WEAKENED, 40 to TIRED, 15 to UNSTABLE),
				(310 to 375) to weightedListOf(               35 to WEAKENED, 50 to TIRED, 15 to UNSTABLE)
			).shuffled(rand)
			
			return Array(amount){
				val (capacity, health) = tiers[it % tiers.size]
				SimpleGenerator(level, capacity, health)
			}
		}
	}
}
