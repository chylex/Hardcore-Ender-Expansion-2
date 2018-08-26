package chylex.hee.init
import gnu.trove.impl.Constants
import gnu.trove.map.hash.TObjectShortHashMap
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ModCreativeTabs{
	lateinit var main: OrderedCreativeTab
	
	fun initialize(){
		main = OrderedCreativeTab("hee")
	}
	
	class OrderedCreativeTab(label: String) : CreativeTabs(label){
		private val itemOrder = TObjectShortHashMap<Item>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, Short.MAX_VALUE)
		
		fun registerOrder(item: Item){
			itemOrder.put(item, (itemOrder.size() + 1).toShort())
		}
		
		@SideOnly(Side.CLIENT)
		override fun getTabIconItem() = ItemStack(ModItems.ETHEREUM)
		
		@SideOnly(Side.CLIENT)
		override fun displayAllRelevantItems(items: NonNullList<ItemStack>){
			super.displayAllRelevantItems(items)
			items.sortWith(compareBy({ it.item is ItemBlock }, { itemOrder[it.item] }))
		}
	}
}
