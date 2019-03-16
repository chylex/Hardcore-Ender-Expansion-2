package chylex.hee.game.mechanics.energy
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.system.util.color.HCL
import kotlin.math.pow

interface IClusterHealth{
	val translationKey: String
	val textColor: Int
	
	val regenAmountMp: Float
	val regenSpeedMp: Float
	val regenCapacityMp: Float
	
	val affectedByProximity: Boolean
	val deterioratesTo: HealthStatus?
	
	fun getLeakChance(cluster: TileEntityEnergyCluster): Float
	
	// Levels
	
	enum class HealthStatus(
		override val translationKey: String, override val textColor: Int,
		override val regenAmountMp: Float, override val regenSpeedMp: Float, override val regenCapacityMp: Float,
		private val canDeteriorate: Boolean = false, private val leakChance: Float = 0F
	): IClusterHealth{
		HEALTHY ("hee.energy.health.healthy",  HCL(120.0, 80F, 74F).toInt(), regenAmountMp = 1.0F,  regenSpeedMp = 1.5F, regenCapacityMp = 1.0F,  canDeteriorate = true),
		WEAKENED("hee.energy.health.weakened", HCL( 75.0, 80F, 74F).toInt(), regenAmountMp = 0.9F,  regenSpeedMp = 1.2F, regenCapacityMp = 1.0F,  canDeteriorate = true),
		TIRED   ("hee.energy.health.tired",    HCL( 45.0, 85F, 70F).toInt(), regenAmountMp = 0.6F,  regenSpeedMp = 0.8F, regenCapacityMp = 0.9F,  canDeteriorate = true),
		DAMAGED ("hee.energy.health.damaged",  HCL( 18.0, 90F, 66F).toInt(), regenAmountMp = 0.4F,  regenSpeedMp = 0.5F, regenCapacityMp = 0.75F, leakChance = 0.5F / 100F),
		UNSTABLE("hee.energy.health.unstable", HCL(  0.0,  0F, 70F).toInt(), regenAmountMp = 0.15F, regenSpeedMp = 0.2F, regenCapacityMp = 0.6F,  leakChance = 1.2F / 100F);
		
		override val affectedByProximity: Boolean = canDeteriorate
		
		override val deterioratesTo: HealthStatus?
			get() = if (canDeteriorate) values().getOrNull(ordinal + 1) else null
		
		override fun getLeakChance(cluster: TileEntityEnergyCluster): Float = leakChance
	}
	
	// Overrides
	
	enum class HealthOverride(
		override val translationKey: String, override val textColor: Int,
		override val regenAmountMp: Float, override val regenSpeedMp: Float = 0F, override val regenCapacityMp: Float
	): IClusterHealth{
		POWERED("hee.energy.health.powered", HCL(210.0, 80F, 66F).toInt(), regenAmountMp = 1.0F, regenSpeedMp = 2.0F, regenCapacityMp = 1.25F){
			override fun getLeakChance(cluster: TileEntityEnergyCluster): Float =
				if (cluster.energyLevel > cluster.energyBaseCapacity)
					(0.1F * (10F * ((cluster.energyLevel.floating.value / cluster.energyBaseCapacity.floating.value) - 1F)).pow(2)) / 100F
				else
					0F
		},
		
		REVITALIZING("hee.energy.health.revitalizing", HCL(320.0, 75F, 66F).toInt(), regenAmountMp = 0.0F, regenSpeedMp = 0.0F, regenCapacityMp = 0.0F){
			override fun getLeakChance(cluster: TileEntityEnergyCluster): Float = 0F
		};
		
		override val affectedByProximity: Boolean = false
		override val deterioratesTo: HealthStatus? = null
	}
}
