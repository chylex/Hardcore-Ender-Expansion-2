package chylex.hee.game.world.structure
import chylex.hee.HEE
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import java.util.Random

interface IBlockPicker{
	fun pick(rand: Random): IBlockState
	
	// Implementations
	
	open class Single(private val state: IBlockState) : IBlockPicker{
		constructor(block: Block) : this(block.defaultState)
		
		override fun pick(rand: Random) = state
		override fun toString() = state.toString()
		
		object Air : Single(Blocks.AIR)
	}
	
	class Weighted private constructor(private val weightedList: WeightedList<IBlockState>) : IBlockPicker{
		companion object{
			fun <T : Comparable<T>> Weighted(baseState: IBlockState, property: IProperty<T>, values: List<Pair<Int, T>>) : Weighted{
				return Weighted(WeightedList(values.map { (weight, value) -> Pair(weight, baseState.with(property, value)) }))
			}
			
			fun <T : Comparable<T>> Weighted(block: Block, property: IProperty<T>, values: List<Pair<Int, T>>) : Weighted{
				return Weighted(block.defaultState, property, values)
			}
			
			fun Weighted(vararg items: Pair<Int, IBlockState>): Weighted{
				return Weighted(WeightedList(items.toList()))
			}
		}
		
		override fun pick(rand: Random) = weightedList.generateItem(rand)
		override fun toString() = weightedList.toString()
	}
	
	class Fallback(private val logMessage: String, state: IBlockState) : Single(state){
		constructor(logMessage: String, block: Block) : this(logMessage, block.defaultState)
		
		override fun pick(rand: Random): IBlockState{
			HEE.log.warn(logMessage)
			return super.pick(rand)
		}
	}
}
