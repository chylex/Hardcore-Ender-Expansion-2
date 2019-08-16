package chylex.hee.game.block.util
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.util.EnumFacing
import net.minecraft.util.IStringSerializable

@Suppress("NOTHING_TO_INLINE")
object Property{
	inline fun bool(name: String): PropertyBool{
		return PropertyBool.create(name)
	}
	
	inline fun int(name: String, range: IntRange): PropertyInteger{
		return PropertyInteger.create(name, range.first, range.last)
	}
	
	inline fun <reified T> enum(name: String): PropertyEnum<T> where T : Enum<T>, T : IStringSerializable{
		return PropertyEnum.create(name, T::class.java)
	}
	
	inline fun facing(name: String, values: Collection<EnumFacing>): PropertyDirection{
		return PropertyDirection.create(name, values)
	}
}
