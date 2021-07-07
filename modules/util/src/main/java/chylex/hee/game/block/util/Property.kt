package chylex.hee.game.block.util

import net.minecraft.state.BooleanProperty
import net.minecraft.state.DirectionProperty
import net.minecraft.state.EnumProperty
import net.minecraft.state.IntegerProperty
import net.minecraft.util.Direction
import net.minecraft.util.IStringSerializable

object Property {
	fun bool(name: String): BooleanProperty {
		return BooleanProperty.create(name)
	}
	
	fun int(name: String, range: IntRange): IntegerProperty {
		return IntegerProperty.create(name, range.first, range.last)
	}
	
	inline fun <reified T> enum(name: String): EnumProperty<T> where T : Enum<T>, T : IStringSerializable {
		return EnumProperty.create(name, T::class.java)
	}
	
	fun facing(name: String, values: Collection<Direction>): DirectionProperty {
		return DirectionProperty.create(name, values)
	}
}
