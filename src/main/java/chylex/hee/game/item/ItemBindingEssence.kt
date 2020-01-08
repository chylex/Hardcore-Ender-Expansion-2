package chylex.hee.game.item
import chylex.hee.client.render.util.NO_TINT
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentString
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.color.IntColor.Companion.RGB
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

class ItemBindingEssence(properties: Properties) : ItemAbstractInfusable(properties){
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return true
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>){
		if (isInGroup(tab)){
			items.add(ItemStack(this))
			
			for(infusion in Infusion.values()){
				items.add(ItemStack(this).also { InfusionTag.setList(it, InfusionList(infusion)) })
			}
		}
	}
	
	override fun getRarity(stack: ItemStack): Rarity{
		return Rarity.UNCOMMON
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		
		val list = InfusionTag.getList(stack)
		
		if (list.isEmpty){
			return
		}
		
		val applicableTo = list
			.flatMap { it.targetItems.asIterable() }
			.groupingBy { it }
			.eachCount()
			.entries
			.sortedWith(compareBy({ -it.value }, { getIdFromItem(it.key) }))
		
		lines.add(TextComponentString(""))
		lines.add(TextComponentTranslation("hee.infusions.applicable.title"))
		
		for((item, count) in applicableTo){
			lines.add(TextComponentTranslation("hee.infusions.applicable.item", item.getDisplayName(ItemStack(item)), count))
		}
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return false
	}
	
	@Sided(Side.CLIENT)
	object Color : IItemColor{
		private val EMPTY = RGB(255u).i
		
		override fun getColor(stack: ItemStack, tintIndex: Int): Int{
			val list = InfusionTag.getList(stack).toList()
			
			if (list.isEmpty()){
				return EMPTY
			}
			
			return when(tintIndex){
				0 -> list[0].primaryColor.i
				1 -> (list.getOrNull(1)?.primaryColor ?: list[0].secondaryColor).i
				2 -> (list.getOrNull(2)?.primaryColor ?: list.getOrNull(1)?.secondaryColor ?: list[0].primaryColor).i
				3 -> (list.getOrNull(3)?.primaryColor ?: list.getOrNull(2)?.primaryColor ?: (if (list.size == 2) list[0].secondaryColor else list[0].primaryColor)).i
				else -> NO_TINT
			}
		}
	}
}
