package chylex.hee.game.mechanics.table.interfaces
import chylex.hee.system.util.size
import net.minecraft.item.ItemStack

interface ITableInputTransformer{
	fun transform(stack: ItemStack)
	
	companion object{
		@JvmField val CONSUME_STACK = object : ITableInputTransformer{
			override fun transform(stack: ItemStack){
				stack.size = 0
			}
		}
		
		@JvmField val CONSUME_ONE = object : ITableInputTransformer{
			override fun transform(stack: ItemStack){
				stack.shrink(1)
			}
		}
	}
}
