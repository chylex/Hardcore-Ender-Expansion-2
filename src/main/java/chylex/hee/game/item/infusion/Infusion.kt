package chylex.hee.game.item.infusion
import chylex.hee.game.render.util.HCL
import chylex.hee.game.render.util.IColor
import chylex.hee.game.render.util.RGB
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.size
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

enum class Infusion(
	val translationKey: String,
	val primaryColor: Int,
	val secondaryColor: Int,
	val targetItems: Array<out Item>
){
	POWER   ("hee.infusion.power",    Colors(primaryHue =  15.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	FIRE    ("hee.infusion.fire",     Colors(primaryHue =  30.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	TRAP    ("hee.infusion.trap",     Colors(primaryHue = 340.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	HARMLESS("hee.infusion.harmless", Colors(primaryHue = 180.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	PHASING ("hee.infusion.phasing",  Colors(primaryHue = 285.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	MINING  ("hee.infusion.mining",   Colors(primaryHue =  70.0, secondaryBrightness = 140u), Matching(ModBlocks.INFUSED_TNT)),
	
	STABILITY("hee.infusion.stability", Colors(primaryHue = 130.0, secondaryHue = 275.0), Matching(ModItems.ENERGY_RECEPTACLE)),
	SAFETY   ("hee.infusion.safety",    Colors(primaryHue = 180.0, secondaryHue = 275.0), Matching(ModItems.ENERGY_RECEPTACLE)),
	
	EXPANSION("hee.infusion.expansion", Colors(primaryHue = 55.0, secondaryCustom = HCL(45.0, 80F, 60F)), Matching(ModItems.TRINKET_POUCH));
	
	// Construction helpers
	
	constructor(translationKey: String, colors: Colors, matching: Matching) : this(translationKey, colors.primary.toInt(), colors.secondary.toInt(), matching.items)
	
	private class Colors(val primary: IColor, val secondary: IColor){
		constructor(primaryHue: Double, secondaryHue: Double) : this(HCL(primaryHue, 100F, 75F), HCL(secondaryHue, 100F, 75F))
		constructor(primaryHue: Double, secondaryCustom: IColor) : this(HCL(primaryHue, 100F, 75F), secondaryCustom)
		constructor(primaryHue: Double, secondaryBrightness: UByte) : this(primaryHue, RGB(secondaryBrightness))
	}
	
	private class Matching(vararg val items: Item){
		constructor(block: Block, vararg items: Item) : this(*arrayOf(Item.getItemFromBlock(block)).plus(items))
	}
	
	// Infusion logic
	
	private companion object{
		private val TRANSFORMATIONS = arrayOf(
			Item.getItemFromBlock(Blocks.TNT) to Item.getItemFromBlock(ModBlocks.INFUSED_TNT)
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
			InfusionTag.setList(it, list.with(this))
		}
	}
}
