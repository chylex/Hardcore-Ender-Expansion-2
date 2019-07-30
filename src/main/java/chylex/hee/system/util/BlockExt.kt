@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.util
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs

// General

val Block.translationKeyOriginal
	get() = this.translationKey.removePrefix("tile.") // UPDATE: there must be a better way?

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
var Block.creativeTabIn: CreativeTabs?
	get() = this.creativeTab
	set(value){ this.creativeTab = value }

// Properties

inline fun <T : Comparable<T>, V : T> IBlockState.with(property: IProperty<T>, value: V): IBlockState{
	return this.withProperty(property, value)
}

inline fun <T : Comparable<T>, V : T> Block.with(property: IProperty<T>, value: V): IBlockState{
	return this.defaultState.withProperty(property, value)
}

inline operator fun <T : Comparable<T>> IBlockState.get(property: IProperty<T>): T{
	return this.getValue(property)
}

inline operator fun IBlockState.get(property: PropertyBool): Boolean{
	return this.getValue(property)
}

inline operator fun IBlockState.get(property: PropertyInteger): Int{
	return this.getValue(property)
}
