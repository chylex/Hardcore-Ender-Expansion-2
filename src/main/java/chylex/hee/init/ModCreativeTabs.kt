package chylex.hee.init

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import it.unimi.dsi.fastutil.objects.Reference2ShortOpenHashMap
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

object ModCreativeTabs {
	val main = OrderedCreativeTab("hee")
	
	class OrderedCreativeTab(label: String) : ItemGroup(label) {
		private val itemOrder = Reference2ShortOpenHashMap<Item>().apply { defaultReturnValue(Short.MAX_VALUE) }
		
		fun registerOrder(item: Item) {
			@Suppress("ReplacePutWithAssignment")
			itemOrder.put(item, (itemOrder.size + 1).toShort()) // kotlin indexer boxes the values
		}
		
		@Sided(Side.CLIENT)
		override fun createIcon() = ItemStack(ModItems.ETHEREUM)
		
		@Sided(Side.CLIENT)
		override fun fill(items: NonNullList<ItemStack>) {
			super.fill(items)
			
			items.sortWith(compareBy(
				{ it.item === ModItems.BINDING_ESSENCE },
				{ it.item === ModItems.PORTAL_TOKEN },
				{ it.item is BlockItem },
				{ itemOrder.getShort(it.item) }
			))
		}
	}
}
