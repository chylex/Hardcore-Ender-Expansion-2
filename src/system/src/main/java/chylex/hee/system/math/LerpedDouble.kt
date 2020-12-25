package chylex.hee.system.math

data class LerpedDouble(var currentValue: Double) : Comparable<LerpedDouble> {
	private var previousValue = currentValue
	
	fun get(partialTicks: Float): Double {
		return previousValue + partialTicks * (currentValue - previousValue)
	}
	
	fun update(newValue: Double) {
		previousValue = currentValue
		currentValue = newValue
	}
	
	fun updateImmediately(newValue: Double) {
		previousValue = newValue
		currentValue = newValue
	}
	
	override fun compareTo(other: LerpedDouble): Int {
		return currentValue.compareTo(other.currentValue)
	}
	
	// Arithmetic operators
	
	operator fun plus(value: Double)  = currentValue + value
	operator fun minus(value: Double) = currentValue - value
	operator fun times(value: Double) = currentValue * value
	operator fun div(value: Double)   = currentValue / value
	
	operator fun plusAssign(value: Double)  = update(currentValue + value)
	operator fun minusAssign(value: Double) = update(currentValue - value)
	operator fun timesAssign(value: Double) = update(currentValue * value)
	operator fun divAssign(value: Double)   = update(currentValue / value)
}
