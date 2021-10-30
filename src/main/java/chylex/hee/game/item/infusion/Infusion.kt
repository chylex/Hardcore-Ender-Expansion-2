package chylex.hee.game.item.infusion

import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Gray
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hcl
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hue
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.item.util.nbtOrNull
import chylex.hee.game.item.util.size
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.util.color.HCL
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.IItemProvider
import java.util.Locale

enum class Infusion(
	val localizedName: String,
	val translationKey: String,
	val primaryColor: IntColor,
	val secondaryColor: IntColor,
	val targetItems: Array<out Item>,
) {
	POWER   ("Power",    Colors(primary = Hcl( 15, l = 60F), secondary = Gray(144u)),                 Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	FIRE    ("Fire",     Colors(primary = Hue( 35), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	TRAP    ("Trap",     Colors(primary = Hue(340), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	MINING  ("Mining",   Colors(primary = Hue( 70), secondary = Gray(144u)),                          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT)),
	HARMLESS("Harmless", Colors(primary = Hue(180), secondary = Hcl(165, c = 10F, l = 70F)),          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT, Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	PHASING ("Phasing",  Colors(primary = Hue(285), secondary = Hcl(165, c = 10F, l = 70F)),          Matching(Blocks.TNT, ModBlocks.INFUSED_TNT, Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	SLOW    ("Slow",     Colors(primary = Hue(110), secondary = Hcl(165, c = 32F, l = 70F)),          Matching(Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	RIDING  ("Riding",   Colors(primary = Hcl( 82, c = 50F), secondary = Hcl(165, c = 32F, l = 70F)), Matching(Items.ENDER_PEARL, ModItems.INFUSED_ENDER_PEARL)),
	
	VIGOR   ("Vigor",    Colors(primary = Gray(244u), secondary = Hue(115)),        Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	CAPACITY("Capacity", Colors(primary = Hue(350), secondary = Hue(210)),          Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	DISTANCE("Distance", Colors(primary = Hcl( 70, l = 85F), secondary = Hue(210)), Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	SPEED   ("Speed",    Colors(primary = Hue( 28), secondary = Hue(210)),          Matching(ModItems.SPATIAL_DASH_GEM)),
	
	STABILITY("Stability", Colors(primary = Hue(130), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	SAFETY   ("Safety",    Colors(primary = Hue(180), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	
	EXPANSION("Expansion", Colors(primary = Hue(55), secondary = Hcl(45, c = 80F, l = 60F)), Matching(ModItems.TRINKET_POUCH));
	
	// Construction helpers
	
	constructor(name: String, colors: Colors, matching: Matching) : this(name, "hee.infusion." + name.lowercase(), colors.primary, colors.secondary, matching.items)
	
	private class Colors(val primary: IntColor, val secondary: IntColor) {
		@Suppress("NOTHING_TO_INLINE", "FunctionName")
		companion object {
			inline fun Hcl(hue: Int, c: Float = 100F, l: Float = 75F) = HCL(hue.toDouble(), c, l)
			inline fun Hue(hue: Int) = Hcl(hue)
			inline fun Gray(brightness: UByte) = RGB(brightness)
		}
	}
	
	private class Matching(vararg val items: Item) {
		constructor(vararg items: IItemProvider) : this(*items.map(IItemProvider::asItem).toTypedArray())
	}
	
	// Infusion logic
	
	companion object {
		fun byName(name: String): Infusion {
			return valueOf(name.uppercase(Locale.ROOT))
		}
		
		fun isInfusable(item: Item): Boolean {
			return item.getHeeInterface<IInfusableItem>() != null || TRANSFORMATIONS.any { it.first === item }
		}
		
		private val TRANSFORMATIONS = arrayOf(
			ModItems.ANCIENT_DUST to ModItems.BINDING_ESSENCE,
			Blocks.TNT.asItem() to ModBlocks.INFUSED_TNT.asItem(),
			Items.ENDER_PEARL to ModItems.INFUSED_ENDER_PEARL
		)
	}
	
	fun tryInfuse(stack: ItemStack): ItemStack? {
		val originalItem = stack.item
		val originalInfusable = originalItem.getHeeInterface<IInfusableItem>()
		if (originalInfusable != null) {
			return tryInfuseTransformed(stack, originalItem, originalInfusable)
		}
		
		val transformedItem = TRANSFORMATIONS.find { it.first === originalItem }?.second
		val transformedInfusable = transformedItem?.getHeeInterface<IInfusableItem>()
		if (transformedInfusable != null) {
			return tryInfuseTransformed(stack, transformedItem, transformedInfusable)
		}
		
		return null
	}
	
	private fun tryInfuseTransformed(stack: ItemStack, transformedItem: Item, transformedInfusable: IInfusableItem): ItemStack? {
		val list = InfusionTag.getList(stack)
		
		if (list.has(this) || !transformedInfusable.canApplyInfusion(transformedItem, this)) {
			return null
		}
		
		return ItemStack(transformedItem, stack.size).also {
			it.tag = stack.nbtOrNull?.copy()
			InfusionTag.setList(it, list.with(this))
		}
	}
}
