package chylex.hee.game.block.builder

import chylex.hee.game.block.util.BlockStateGenerics
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.Property
import net.minecraft.state.StateContainer
import net.minecraft.util.Direction
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation

class HeeBlockStates private constructor(private val statesWithDefaults: Map<Property<*>, Comparable<*>>, private val facingProperty: Property<Direction>?) {
	fun applyDefaults(state: BlockState): BlockState {
		if (statesWithDefaults.isEmpty()) {
			return state
		}
		
		return statesWithDefaults.entries.fold(state) { acc, entry ->
			BlockStateGenerics.withProperty(acc, entry.key, entry.value)
		}
	}
	
	fun getRotatedState(state: BlockState, rotation: Rotation): BlockState {
		return facingProperty?.let { state.with(it, rotation.rotate(state[it])) } ?: state
	}
	
	fun getMirroredState(state: BlockState, mirror: Mirror): BlockState {
		return facingProperty?.let { state.with(it, mirror.mirror(state[it])) } ?: state
	}
	
	class Builder {
		private val statesWithDefaults = mutableMapOf<Property<*>, Comparable<*>>()
		
		var facingProperty: Property<Direction>? = null
		
		fun <T : Comparable<T>> set(state: Property<T>, default: T) {
			statesWithDefaults[state] = default
		}
		
		fun fillContainer(builder: StateContainer.Builder<Block, BlockState>) {
			for (property in statesWithDefaults.keys) {
				builder.add(property)
			}
		}
		
		fun build(): HeeBlockStates {
			return HeeBlockStates(statesWithDefaults.toMap(), facingProperty)
		}
	}
}
