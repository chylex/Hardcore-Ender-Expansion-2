package chylex.hee.game.item.infusion
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Gray
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hcl
import chylex.hee.game.item.infusion.Infusion.Colors.Companion.Hue
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.color.HCL
import chylex.hee.system.util.color.IColor
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.size
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.util.Locale

enum class Infusion(
	val translationKey: String,
	val primaryColor: Int,
	val secondaryColor: Int,
	val targetItems: Array<out Item>
){
	POWER   (Name("power"),    Colors(primary = Hcl( 15, l = 60F), secondary = Gray(144u)),        Matching(ModBlocks.INFUSED_TNT)),
	FIRE    (Name("fire"),     Colors(primary = Hue( 35), secondary = Gray(144u)),                 Matching(ModBlocks.INFUSED_TNT)),
	TRAP    (Name("trap"),     Colors(primary = Hue(340), secondary = Gray(144u)),                 Matching(ModBlocks.INFUSED_TNT)),
	MINING  (Name("mining"),   Colors(primary = Hue( 70), secondary = Gray(144u)),                 Matching(ModBlocks.INFUSED_TNT)),
	HARMLESS(Name("harmless"), Colors(primary = Hue(180), secondary = Hcl(165, c = 10F, l = 70F)), Matching(ModBlocks.INFUSED_TNT, ModItems.INFUSED_ENDER_PEARL)),
	PHASING (Name("phasing"),  Colors(primary = Hue(285), secondary = Hcl(165, c = 10F, l = 70F)), Matching(ModBlocks.INFUSED_TNT, ModItems.INFUSED_ENDER_PEARL)),
	SLOW    (Name("slow"),     Colors(primary = Hue(110), secondary = Hcl(165, c = 32F, l = 70F)), Matching(ModItems.INFUSED_ENDER_PEARL)),
	RIDING  (Name("riding"),   Colors(primary = Hcl( 82, c = 50F), secondary = Hcl(165, c = 32F, l = 70F)), Matching(ModItems.INFUSED_ENDER_PEARL)),
	
	VIGOR   (Name("vigor"),    Colors(primary = Gray(244u), secondary = Hue(115)),        Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	CAPACITY(Name("capacity"), Colors(primary = Hue(350), secondary = Hue(210)),          Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	DISTANCE(Name("distance"), Colors(primary = Hcl( 70, l = 85F), secondary = Hue(210)), Matching(ModItems.ENERGY_ORACLE, ModItems.SPATIAL_DASH_GEM)),
	SPEED   (Name("speed"),    Colors(primary = Hue( 28), secondary = Hue(210)),          Matching(ModItems.SPATIAL_DASH_GEM)),
	
	STABILITY(Name("stability"), Colors(primary = Hue(130), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	SAFETY   (Name("safety"),    Colors(primary = Hue(180), secondary = Hue(275)), Matching(ModItems.ENERGY_RECEPTACLE)),
	
	EXPANSION(Name("expansion"), Colors(primary = Hue(55), secondary = Hcl(45, c = 80F, l = 60F)), Matching(ModItems.TRINKET_POUCH));
	
	// Construction helpers
	
	constructor(name: Name, colors: Colors, matching: Matching) : this("hee.infusion.${name.name}", colors.primary.toInt(), colors.secondary.toInt(), matching.items)
	
	private class Name(val name: String)
	
	private class Colors(val primary: IColor, val secondary: IColor){
		@Suppress("NOTHING_TO_INLINE")
		companion object{
			inline fun Hcl(hue: Int, c: Float = 100F, l: Float = 75F) = HCL(hue.toDouble(), c, l)
			inline fun Hue(hue: Int) = Hcl(hue)
			inline fun Gray(brightness: UByte) = RGB(brightness)
		}
	}
	
	private class Matching(vararg val items: Item){
		constructor(block: Block, vararg items: Item) : this(*arrayOf(Item.getItemFromBlock(block)).plus(items))
	}
	
	// Infusion logic
	
	companion object{
		fun byName(name: String): Infusion{
			return valueOf(name.toUpperCase(Locale.ROOT))
		}
		
		private val TRANSFORMATIONS = arrayOf(
			Item.getItemFromBlock(Blocks.TNT) to Item.getItemFromBlock(ModBlocks.INFUSED_TNT),
			Items.ENDER_PEARL to ModItems.INFUSED_ENDER_PEARL
		)
	}
	
	fun tryInfuse(stack: ItemStack): ItemStack?{
		val originalItem = stack.item
		
		if (originalItem is IInfusableItem){
			return tryInfuseTransformed(stack, originalItem, originalItem)
		}
		
		val transformedItem = TRANSFORMATIONS.find { it.first === originalItem }?.second
		
		if (transformedItem is IInfusableItem){
			return tryInfuseTransformed(stack, transformedItem, transformedItem)
		}
		
		return null
	}
	
	private fun tryInfuseTransformed(stack: ItemStack, transformedItem: Item, transformedInfusable: IInfusableItem): ItemStack?{
		val list = InfusionTag.getList(stack)
		
		if (list.has(this) || !transformedInfusable.canApplyInfusion(this)){
			return null
		}
		
		return ItemStack(transformedItem, stack.size, stack.metadata).also {
			it.tagCompound = stack.nbtOrNull?.copy()
			InfusionTag.setList(it, list.with(this))
		}
	}
}
