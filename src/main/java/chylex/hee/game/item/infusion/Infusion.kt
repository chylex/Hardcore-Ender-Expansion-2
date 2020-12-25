package chylex.hee.game.item.infusion

import chylex.hee.game.inventory.nbtOrNull
import chylex.hee.game.inventory.size
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Gray
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hcl
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hue
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.HCL
import chylex.hee.system.color.IntColor.Companion.RGB
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.IItemProvider
import java.util.Locale

enum class Infusion(
	val translationKey: String,
	val primaryColor: IntColor,
	val secondaryColor: IntColor,
	val targetItems: Array<out Item>
) {
	POWER   ("power",    Colors(primary = Hcl( 15, l = 60F), secondary = Gray(144u)),                 Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	FIRE    ("fire",     Colors(primary = Hue( 35), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	TRAP    ("trap",     Colors(primary = Hue(340), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	MINING  ("mining",   Colors(primary = Hue( 70), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	HARMLESS("harmless", Colors(primary = Hue(180), secondary = Hcl(165, c = 10F, l = 70F)),          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT, Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	PHASING ("phasing",  Colors(primary = Hue(285), secondary = Hcl(165, c = 10F, l = 70F)),          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT, Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	SLOW    ("slow",     Colors(primary = Hue(110), secondary = Hcl(165, c = 32F, l = 70F)),          Matching(Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	RIDING  ("riding",   Colors(primary = Hcl( 82, c = 50F), secondary = Hcl(165, c = 32F, l = 70F)), Matching(Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	
	VIGOR   ("vigor",    Colors(primary = Gray(244u), secondary = Hue(115)),        Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	CAPACITY("capacity", Colors(primary = Hue(350), secondary = Hue(210)),          Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	DISTANCE("distance", Colors(primary = Hcl( 70, l = 85F), secondary = Hue(210)), Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	SPEED   ("speed",    Colors(primary = Hue( 28), secondary = Hue(210)),          Matching(ModItems.SPATIAL_DASH_GEM)),
	
	STABILITY("stability", Colors(primary = Hue(130), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	SAFETY   ("safety",    Colors(primary = Hue(180), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	
	EXPANSION("expansion", Colors(primary = Hue(55), secondary = Hcl(45, c = 80F, l = 60F)), Matching(ModItems.TRINKET_POUCH));
	
	// Construction helpers
	
	constructor(name: String, colors: Colors, matching: Matching) : this("hee.infusion.${name}", colors.primary, colors.secondary, matching.items)
	
	private class Colors(val primary: IntColor, val secondary: IntColor) {
		@Suppress("NOTHING_TO_INLINE", "FunctionName")
		companion object {
			inline fun Hcl(hue: Int, c: Float = 100F, l: Float = 75F) = HCL(hue.toDouble(), c, l)
			inline fun Hue(hue: Int) = Hcl(hue)
			inline fun Gray(brightness: UByte) = RGB(brightness)
		}
	}
	
	private class Matching(vararg val items: Item) {
		constructor(vararg items: IItemProvider) : this(*items.map { it.asItem() }.toTypedArray())
	}
	
	// Infusion logic
	
	companion object {
		fun byName(name: String): Infusion {
			return valueOf(name.toUpperCase(Locale.ROOT))
		}
		
		fun isInfusable(item: Item): Boolean {
			return item is IInfusableItem || TRANSFORMATIONS.any { it.first === item }
		}
		
		private val TRANSFORMATIONS = arrayOf(
			ModItems.ANCIENT_DUST to ModItems.BINDING_ESSENCE,
			Blocks.TNT.asItem() to ModBlocks.INFUSED_TNT.asItem(),
			Items.ENDER_PEARL to ModItems.INFUSED_ENDER_PEARL
		)
	}
	
	fun tryInfuse(stack: ItemStack): ItemStack? {
		val originalItem = stack.item
		
		if (originalItem is IInfusableItem) {
			return tryInfuseTransformed(stack, originalItem, originalItem)
		}
		
		val transformedItem = TRANSFORMATIONS.find { it.first === originalItem }?.second
		
		if (transformedItem is IInfusableItem) {
			return tryInfuseTransformed(stack, transformedItem, transformedItem)
		}
		
		return null
	}
	
	private fun tryInfuseTransformed(stack: ItemStack, transformedItem: Item, transformedInfusable: IInfusableItem): ItemStack? {
		val list = InfusionTag.getList(stack)
		
		if (list.has(this) || !transformedInfusable.canApplyInfusion(this)) {
			return null
		}
		
		return ItemStack(transformedItem, stack.size).also {
			it.tag = stack.nbtOrNull?.copy()
			InfusionTag.setList(it, list.with(this))
		}
	}
}
