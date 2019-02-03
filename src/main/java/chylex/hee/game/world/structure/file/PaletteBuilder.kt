package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.system.util.compatibility.EraseGenerics
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import org.apache.commons.lang3.StringUtils

class PaletteBuilder{
	private val palette = mutableMapOf<String, IBlockPicker>()
	
	// Add one
	
	fun add(key: String, picker: IBlockPicker){
		palette[key] = picker
	}
	
	fun add(key: String, state: IBlockState) = add(key, Single(state))
	fun add(key: String, block: Block) = add(key, Single(block))
	
	// Add one variant
	
	fun <T : Comparable<T>> add(key: String, baseState: IBlockState, propertyMapping: Pair<IProperty<T>, Map<String, T>>){
		val (property, mapping) = propertyMapping
		
		for((name, value) in mapping){
			add(updateKey(key, name), baseState.withProperty(property, value))
		}
	}
	
	fun <T : Comparable<T>> add(key: String, block: Block, propertyMapping: Pair<IProperty<T>, Map<String, T>>) = add(key, block.defaultState, propertyMapping)
	
	// Add cross product of variants
	
	fun add(key: String, baseState: IBlockState, propertyMappings: List<Pair<IProperty<*>, Map<String, *>>>){
		fun process(index: Int, currentKey: String, currentState: IBlockState){
			if (index == propertyMappings.size){
				add(currentKey, currentState)
				return
			}
			
			val (property, mapping) = propertyMappings[index]
			
			for((name, value) in mapping){
				process(index + 1, updateKey(currentKey, name), EraseGenerics.withProperty(currentState, property, value as Comparable<*>))
			}
		}
		
		process(0, key, baseState)
	}
	
	fun add(key: String, block: Block, propertyMappings: List<Pair<IProperty<*>, Map<String, *>>>) = add(key, block.defaultState, propertyMappings)
	
	// Utilities
	
	private fun updateKey(key: String, name: String): String{
		return if (key.contains(".*"))
			StringUtils.replaceOnce(key, ".*", if (name.isEmpty()) "" else ".$name")
		else
			throw IllegalArgumentException("missing replacement wildcard in palette key: $key")
	}
	
	// Build
	
	fun build(): Map<String, IBlockPicker>{
		return palette.toMap()
	}
}
