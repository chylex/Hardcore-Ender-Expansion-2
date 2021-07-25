package chylex.hee.game.entity.properties

data class EntityTrackerInfo(
	val trackingRange: Int,
	val updateInterval: Int,
	val receiveVelocityUpdates: Boolean,
) {
	object Defaults {
		val MOB           = EntityTrackerInfo(trackingRange =  5, updateInterval =  3, receiveVelocityUpdates = true)
		val ITEM          = EntityTrackerInfo(trackingRange =  4, updateInterval =  3, receiveVelocityUpdates = true)
		val PROJECTILE    = EntityTrackerInfo(trackingRange =  4, updateInterval = 10, receiveVelocityUpdates = true)
		val FALLING_BLOCK = EntityTrackerInfo(trackingRange = 10, updateInterval = 20, receiveVelocityUpdates = true)
		val TECHNICAL     = EntityTrackerInfo(trackingRange =  0, updateInterval = Int.MAX_VALUE, receiveVelocityUpdates = false)
	}
}
