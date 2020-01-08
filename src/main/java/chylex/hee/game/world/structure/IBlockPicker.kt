package chylex.hee.game.world.structure
import chylex.hee.HEE
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.IProperty
import java.util.Random

interface IBlockPicker{
	fun pick(rand: Random): BlockState
	
	// Implementations
	
	open class Single(private val state: BlockState) : IBlockPicker{
		constructor(block: Block) : this(block.defaultState)
		
		override fun pick(rand: Random) = state
		override fun toString() = state.toString()
		
		object Air : Single(Blocks.AIR)
	}
	
	class Weighted private constructor(private val weightedList: WeightedList<BlockState>) : IBlockPicker{
		companion object{
			fun <T : Comparable<T>> Weighted(baseState: BlockState, property: IProperty<T>, values: List<Pair<Int, T>>) : Weighted{
				return Weighted(WeightedList(values.map { (weight, value) -> Pair(weight, baseState.with(property, value)) }))
			}
			
			fun <T : Comparable<T>> Weighted(block: Block, property: IProperty<T>, values: List<Pair<Int, T>>) : Weighted{
				return Weighted(block.defaultState, property, values)
			}
			
			@JvmName("Weighted_BlockState")
			fun Weighted(vararg items: Pair<Int, BlockState>): Weighted{
				return Weighted(WeightedList(items.toList()))
			}
			
			@JvmName("Weighted_Block")
			fun Weighted(vararg items: Pair<Int, Block>): Weighted{
				return Weighted(WeightedList(items.toList().map { it.first to it.second.defaultState }))
			}
		}
		
		override fun pick(rand: Random) = weightedList.generateItem(rand)
		override fun toString() = weightedList.toString()
	}
	
	class Fallback(private val logMessage: String, state: BlockState) : Single(state){
		constructor(logMessage: String, block: Block) : this(logMessage, block.defaultState)
		
		override fun pick(rand: Random): BlockState{
			HEE.log.warn(logMessage)
			return super.pick(rand)
		}
	}
}
