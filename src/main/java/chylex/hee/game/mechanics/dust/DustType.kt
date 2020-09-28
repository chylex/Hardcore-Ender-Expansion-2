package chylex.hee.game.mechanics.dust
import chylex.hee.init.ModItems
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.RGB
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

enum class DustType(val key: String, val item: Item, val color: IntArray){
	STARDUST("stardust", ModItems.STARDUST, RGB(255, 255, 72)),
	END_POWDER("end_powder", ModItems.END_POWDER, RGB(131, 7, 189)),
	ANCIENT_DUST("ancient_dust", ModItems.ANCIENT_DUST, RGB(147, 166, 169)),
	GLOWSTONE_DUST("glowstone_dust", Items.GLOWSTONE_DUST, RGB(210, 210, 0)),
	GUNPOWDER("gunpowder", Items.GUNPOWDER, RGB(114u)),
	REDSTONE("redstone", Items.REDSTONE, RGB(114, 0, 0)),
	SUGAR("sugar", Items.SUGAR, RGB(248, 248, 255));
	
	constructor(key: String, item: Item, color: IntColor) : this(key, item, color.let { intArrayOf(it.red, it.green, it.blue) })
	
	@Suppress("DEPRECATION")
	val maxStackSize
		get() = item.maxStackSize
	
	companion object{
		fun fromStack(stack: ItemStack): DustType?{
			return values().firstOrNull { it.item === stack.item }
		}
	}
}
