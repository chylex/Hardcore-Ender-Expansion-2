package chylex.hee.game.mechanics.energy
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound

class ClusterSnapshot(
	val energyLevel: IEnergyQuantity,
	val energyCapacity: IEnergyQuantity,
	val healthStatus: HealthStatus,
	val healthOverride: HealthOverride?,
	val color: ClusterColor
){
	private companion object{
		const val ENERGY_LEVEL_TAG = "EnergyLevel"
		const val ENERGY_CAPACITY_TAG = "EnergyCapacity"
		const val HEALTH_STATUS_TAG = "HealthStatus"
		const val HEALTH_OVERRIDE_TAG = "HealthOverride"
		const val COLOR_PRIMARY_TAG = "ColorPrimary"
		const val COLOR_SECONDARY_TAG = "ColorSecondary"
	}
	
	val tag
		get() = NBTTagCompound().apply {
			setInteger(ENERGY_LEVEL_TAG, energyLevel.internal.value)
			setInteger(ENERGY_CAPACITY_TAG, energyCapacity.internal.value)
			setEnum(HEALTH_STATUS_TAG, healthStatus)
			setEnum(HEALTH_OVERRIDE_TAG, healthOverride)
			setShort(COLOR_PRIMARY_TAG, color.primaryHue)
			setShort(COLOR_SECONDARY_TAG, color.secondaryHue)
		}
	
	constructor(tag: NBTTagCompound) : this(
		energyLevel    = Internal(tag.getInteger(ENERGY_LEVEL_TAG)),
		energyCapacity = Internal(tag.getInteger(ENERGY_CAPACITY_TAG)),
		healthStatus   = tag.getEnum<HealthStatus>(HEALTH_STATUS_TAG) ?: HEALTHY,
		healthOverride = tag.getEnum<HealthOverride>(HEALTH_OVERRIDE_TAG),
		color          = ClusterColor(tag.getShort(COLOR_PRIMARY_TAG), tag.getShort(COLOR_SECONDARY_TAG))
	)
	
	fun clone(
		energyLevel: IEnergyQuantity    = this.energyLevel,
		energyCapacity: IEnergyQuantity = this.energyCapacity,
		healthStatus: HealthStatus      = this.healthStatus,
		healthOverride: HealthOverride? = this.healthOverride,
		color: ClusterColor             = this.color
	) = ClusterSnapshot(
		energyLevel    = energyLevel,
		energyCapacity = energyCapacity,
		healthStatus   = healthStatus,
		healthOverride = healthOverride,
		color          = color
	)
}
