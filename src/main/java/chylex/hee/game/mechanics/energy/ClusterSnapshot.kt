package chylex.hee.game.mechanics.energy

import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.putEnum

class ClusterSnapshot(
	val energyLevel: IEnergyQuantity,
	val energyCapacity: IEnergyQuantity,
	val healthStatus: HealthStatus,
	val healthOverride: HealthOverride?,
	val color: ClusterColor,
) {
	private companion object {
		private const val ENERGY_LEVEL_TAG = "EnergyLevel"
		private const val ENERGY_CAPACITY_TAG = "EnergyCapacity"
		private const val HEALTH_STATUS_TAG = "HealthStatus"
		private const val HEALTH_OVERRIDE_TAG = "HealthOverride"
		private const val COLOR_PRIMARY_TAG = "ColorPrimary"
		private const val COLOR_SECONDARY_TAG = "ColorSecondary"
	}
	
	val tag
		get() = TagCompound().apply {
			putInt(ENERGY_LEVEL_TAG, energyLevel.internal.value)
			putInt(ENERGY_CAPACITY_TAG, energyCapacity.internal.value)
			putEnum(HEALTH_STATUS_TAG, healthStatus)
			putEnum(HEALTH_OVERRIDE_TAG, healthOverride)
			putShort(COLOR_PRIMARY_TAG, color.primaryHue)
			putShort(COLOR_SECONDARY_TAG, color.secondaryHue)
		}
	
	constructor(tag: TagCompound) : this(
		energyLevel    = Internal(tag.getInt(ENERGY_LEVEL_TAG)),
		energyCapacity = Internal(tag.getInt(ENERGY_CAPACITY_TAG)),
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
