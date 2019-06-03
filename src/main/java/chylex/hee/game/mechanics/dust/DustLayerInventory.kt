package chylex.hee.game.mechanics.dust
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import chylex.hee.system.util.size
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import kotlin.math.max

class DustLayerInventory(private val layers: DustLayers, private val isIntake: Boolean) : IItemHandler{
	override fun getSlots() = layers.contents.size + 1
	override fun getSlotLimit(slot: Int) = layers.totalCapacity
	
	override fun getStackInSlot(slot: Int): ItemStack{
		return layers.contents.getOrNull(slot)?.let { ItemStack(it.first.item, it.second.toInt()) } ?: ItemStack.EMPTY
	}
	
	override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack{
		if (!isIntake){
			return stack
		}
		
		val dustType = DustType.fromStack(stack)
		
		if (dustType == null){
			return stack
		}
		
		return if (simulate)
			stack.copy().also { it.size = max(0, it.size - layers.remainingCapacity) }
		else
			stack.copy().also { it.size -= layers.addDust(dustType, stack.size) }
	}
	
	override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack{
		if (isIntake){
			return ItemStack.EMPTY
		}
		
		return if (simulate)
			layers.getDust(BOTTOM, amount)
		else
			layers.removeDust(BOTTOM, amount)
	}
}
