package chylex.hee.game.block.util
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.util.IStringSerializable

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
}
