package chylex.hee.game.world.structure.palette

import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.system.compatibility.EraseGenerics
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.IProperty
import org.apache.commons.lang3.StringUtils

sealed class PaletteBuilder {
	
	// Add one
	
	abstract fun add(key: String, state: BlockState)
	
	fun add(key: String, block: Block) {
		add(key, block.defaultState)
	}
	
	// Add multiple
	
	fun add(key: String, stateMappings: Map<String, BlockState>) {
		for((suffix, state) in stateMappings) {
			add(updateKey(key, suffix), state)
		}
	}
	
	// Add variants
	
	fun add(key: String, baseState: BlockState, propertyMappings: List<Pair<IProperty<*>, Map<String, *>>>) {
		fun process(index: Int, currentKey: String, currentState: BlockState) {
			if (index == propertyMappings.size) {
				add(currentKey, currentState)
				return
			}
			
			val (property, mapping) = propertyMappings[index]
			
			for((name, value) in mapping) {
				process(index + 1, updateKey(currentKey, name), EraseGenerics.withProperty(currentState, property, value as Comparable<*>))
			}
		}
		
		process(0, key, baseState)
	}
	
	fun add(key: String, block: Block, propertyMappings: List<Pair<IProperty<*>, Map<String, *>>>) {
		add(key, block.defaultState, propertyMappings)
	}
	
	fun <T : Comparable<T>> add(key: String, baseState: BlockState, propertyMapping: Pair<IProperty<T>, Map<String, T>>) {
		add(key, baseState, listOf(propertyMapping))
	}
	
	fun <T : Comparable<T>> add(key: String, block: Block, propertyMapping: Pair<IProperty<T>, Map<String, T>>) {
		add(key, block.defaultState, listOf(propertyMapping))
	}
	
	// Utilities
	
	private fun updateKey(key: String, name: String): String {
		return if (key.contains(".*"))
			StringUtils.replaceOnce(key, ".*", if (name.isEmpty()) "" else ".$name")
		else
			throw IllegalArgumentException("missing replacement wildcard in palette key: $key")
	}
	
	// Generation
	
	class ForGeneration : PaletteBuilder() {
		private val palette = mutableMapOf<String, IBlockPicker>()
		
		fun add(key: String, picker: IBlockPicker) {
			palette[key] = picker
		}
		
		override fun add(key: String, state: BlockState) {
			add(key, Single(state))
		}
		
		fun build(): Map<String, IBlockPicker> {
			return palette.toMap()
		}
	}
	
	// Development
	
	class ForDevelopment : PaletteBuilder() {
		private val palette = HashBiMap.create<String, BlockState>()
		
		override fun add(key: String, state: BlockState) {
			palette[key] = state
		}
		
		fun build(): ImmutableBiMap<String, BlockState> {
			return ImmutableBiMap.copyOf(palette)
		}
	}
	
	// Combined
	
	class Combined : PaletteBuilder() {
		val forGeneration = ForGeneration()
		val forDevelopment = ForDevelopment()
		
		override fun add(key: String, state: BlockState) {
			forGeneration.add(key, state)
			forDevelopment.add(key, state)
		}
		
		fun build(): Palette {
			return Palette(forGeneration.build(), forDevelopment.build())
		}
	}
}
