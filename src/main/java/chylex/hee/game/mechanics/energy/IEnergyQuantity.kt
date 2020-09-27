package chylex.hee.game.mechanics.energy
import chylex.hee.system.math.floorToInt

interface IEnergyQuantity: Comparable<IEnergyQuantity>{
	val internal: Internal
	
	val floating: Floating
		get() = Floating(internal.value / 1_000_000F)
	
	val units: Units
		get() = Units(internal.value / 50_000)
	
	// Operators
	
	operator fun plus(rightSide: IEnergyQuantity): IEnergyQuantity{
		return Internal(internal.value + rightSide.internal.value)
	}
	
	operator fun minus(rightSide: IEnergyQuantity): IEnergyQuantity{
		return Internal(internal.value - rightSide.internal.value)
	}
	
	operator fun times(rightSide: Float): IEnergyQuantity{
		return Internal((internal.value * rightSide).floorToInt())
	}
	
	override fun compareTo(other: IEnergyQuantity): Int{
		return internal.value.compareTo(other.internal.value)
	}
	
	override fun equals(other: Any?): Boolean
	override fun hashCode(): Int
	override fun toString(): String
	
	companion object{
		val MAX_POSSIBLE_VALUE = Units(2000)
		val MAX_REGEN_CAPACITY = Units(1000)
		
		val IEnergyQuantity.displayString
			get() = "%.2f".format((this.floating.value * 100F).floorToInt() * 0.01F)
		
		private fun equals(left: IEnergyQuantity, right: Any?): Boolean{
			return right is IEnergyQuantity && left.internal.value == right.internal.value
		}
		
		private fun hashCode(obj: IEnergyQuantity): Int{
			return obj.internal.value.hashCode()
		}
		
		private fun toString(obj: IEnergyQuantity): String{
			return "Energy (internal = ${obj.internal.value}, floating = ${obj.floating.value}, units = ${obj.units.value})"
		}
	}
	
	/**
	 * Represents a quantity of Energy in a fixed-point 000.###### format suitable for storage.
	 * The value is clamped between 0 and 100(.)000000.
	 */
	class Internal(value: Int): IEnergyQuantity{
		val value = value.coerceIn(0, 100 * 1_000_000)
		
		override val internal: Internal = this
		
		override fun equals(other: Any?) = equals(this, other)
		override fun hashCode() = hashCode(this)
		override fun toString() = toString(this)
	}
	
	/**
	 * Represents a quantity of Energy in a floating point format.
	 * The value is clamped between 0 and 100.
	 */
	class Floating(value: Float): IEnergyQuantity{
		val value = value.coerceIn(0F, 100F)
		
		override val internal: Internal = Internal((value * 1_000_000).floorToInt())
		
		override fun equals(other: Any?) = equals(this, other)
		override fun hashCode() = hashCode(this)
		override fun toString() = toString(this)
	}
	
	/**
	 * Represents the smallest quantity of Energy that can be manipulated at once, equal to 0.05 Energy.
	 * The value is clamped between 0 and 2000.
	 */
	class Units(value: Int): IEnergyQuantity{
		val value = value.coerceIn(0, 2000)
		
		override val internal: Internal = Internal(value * 50_000)
		
		override fun equals(other: Any?) = equals(this, other)
		override fun hashCode() = hashCode(this)
		override fun toString() = toString(this)
	}
}
