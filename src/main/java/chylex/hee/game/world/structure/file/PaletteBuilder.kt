package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.system.util.compatibility.EraseGenerics
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import org.apache.commons.lang3.StringUtils

sealed class PaletteBuilder{
	
	// Add one
	
	abstract fun add(key: String, state: IBlockState)
	
	fun add(key: String, block: Block){
		add(key, block.defaultState)
	}
	
	// Add variants
	
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
	
	fun add(key: String, block: Block, propertyMappings: List<Pair<IProperty<*>, Map<String, *>>>){
		add(key, block.defaultState, propertyMappings)
	}
	
	fun <T : Comparable<T>> add(key: String, baseState: IBlockState, propertyMapping: Pair<IProperty<T>, Map<String, T>>){
		add(key, baseState, listOf(propertyMapping))
	}
	
	fun <T : Comparable<T>> add(key: String, block: Block, propertyMapping: Pair<IProperty<T>, Map<String, T>>){
		add(key, block.defaultState, listOf(propertyMapping))
	}
	
	// Utilities
	
	private fun updateKey(key: String, name: String): String{
		return if (key.contains(".*"))
			StringUtils.replaceOnce(key, ".*", if (name.isEmpty()) "" else ".$name")
		else
			throw IllegalArgumentException("missing replacement wildcard in palette key: $key")
	}
	
	// Generation
	
	class ForGeneration : PaletteBuilder(){
		private val palette = mutableMapOf<String, IBlockPicker>()
		
		fun add(key: String, picker: IBlockPicker){
			palette[key] = picker
		}
		
		override fun add(key: String, state: IBlockState){
			add(key, Single(state))
		}
		
		fun build(): Map<String, IBlockPicker>{
			return palette.toMap()
		}
	}
	
	// Development
	
	class ForDevelopment : PaletteBuilder(){
		private val palette = HashBiMap.create<String, IBlockState>()
		
		override fun add(key: String, state: IBlockState){
			palette[key] = state
		}
		
		fun build(): ImmutableBiMap<String, IBlockState>{
			return ImmutableBiMap.copyOf(palette)
		}
	}
	
	// Combined
	
	class Combined : PaletteBuilder(){
		val forGeneration = ForGeneration()
		val forDevelopment = ForDevelopment()
		
		override fun add(key: String, state: IBlockState){
			forGeneration.add(key, state)
			forDevelopment.add(key, state)
		}
		
		fun build(): Palette{
			return Palette(forGeneration.build(), forDevelopment.build())
		}
	}
}
