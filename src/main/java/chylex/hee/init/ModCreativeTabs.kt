package chylex.hee.init
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap
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
		private val itemOrder = Object2ShortOpenHashMap<Item>().apply { defaultReturnValue(Short.MAX_VALUE) }
		
		fun registerOrder(item: Item){
			itemOrder[item] = (itemOrder.size + 1).toShort()
		}
		
		@SideOnly(Side.CLIENT)
		override fun createIcon() = ItemStack(ModItems.ETHEREUM)
		
		@SideOnly(Side.CLIENT)
		override fun displayAllRelevantItems(items: NonNullList<ItemStack>){
			super.displayAllRelevantItems(items)
			items.sortWith(compareBy({ it.item === ModItems.BINDING_ESSENCE }, { it.item is ItemBlock }, { itemOrder.getShort(it.item) }))
		}
	}
}
