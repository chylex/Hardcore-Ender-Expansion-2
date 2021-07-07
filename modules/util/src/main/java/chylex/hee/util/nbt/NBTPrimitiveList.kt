package chylex.hee.util.nbt

class NBTPrimitiveList(tagList: TagList = TagList()) : NBTList<NBTPrimitive>(tagList) {
	val allBytes
		get() = this.map(NBTPrimitive::getByte)
	
	val allShorts
		get() = this.map(NBTPrimitive::getShort)
	
	val allInts
		get() = this.map(NBTPrimitive::getInt)
	
	val allLongs
		get() = this.map(NBTPrimitive::getLong)
	
	val allFloats
		get() = this.map(NBTPrimitive::getFloat)
	
	val allDoubles
		get() = this.map(NBTPrimitive::getDouble)
	
	fun append(value: Byte)   = tagList.add(TagByte.valueOf(value))
	fun append(value: Short)  = tagList.add(TagShort.valueOf(value))
	fun append(value: Int)    = tagList.add(TagInt.valueOf(value))
	fun append(value: Long)   = tagList.add(TagLong.valueOf(value))
	fun append(value: Float)  = tagList.add(TagFloat.valueOf(value))
	fun append(value: Double) = tagList.add(TagDouble.valueOf(value))
	
	override fun convert(element: NBTPrimitive) = element
	
	override fun get(index: Int) = when (val tag = tagList[index]) {
		is NBTPrimitive -> tag
		is TagEnd       -> throw IndexOutOfBoundsException()
		else            -> throw IllegalArgumentException("unhandled NBT type: ${tag::class.java.simpleName}")
	}
}
