package chylex.hee.system.util.math

data class LerpedFloat(var currentValue: Float) : Comparable<LerpedFloat>{
	private var previousValue = currentValue
	
	fun get(partialTicks: Float): Float{
		return previousValue + partialTicks * (currentValue - previousValue)
	}
	
	fun update(newValue: Float){
		previousValue = currentValue
		currentValue = newValue
	}
	
	fun updateImmediately(newValue: Float){
		previousValue = newValue
		currentValue = newValue
	}
	
	override fun compareTo(other: LerpedFloat): Int{
		return currentValue.compareTo(other.currentValue)
	}
	
	// Arithmetic operators
	
	operator fun plus(value: Float): Float  = currentValue + value
	operator fun minus(value: Float): Float = currentValue - value
	operator fun times(value: Float): Float = currentValue * value
	operator fun div(value: Float): Float   = currentValue / value
	
	operator fun plusAssign(value: Float)  = update(currentValue + value)
	operator fun minusAssign(value: Float) = update(currentValue - value)
	operator fun timesAssign(value: Float) = update(currentValue * value)
	operator fun divAssign(value: Float)   = update(currentValue / value)
}
