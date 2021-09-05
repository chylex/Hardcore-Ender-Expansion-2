package chylex.hee.util.math

@JvmInline
value class FloatRange(private val combined: Long) {
	constructor(start: Float, end: Float) : this((start.toRawBits() shlong 32) or (end.toRawBits().toLong() and 0xFFFF_FFFFL))
	
	val start
		get() = Float.fromBits((combined ushr 32).toInt())
	
	val end
		get() = Float.fromBits((combined and 0xFFFF_FFFFL).toInt())
}

fun range(start: Float, end: Float) = FloatRange(start, end)
