package chylex.hee.game.item
import chylex.hee.game.item.base.ItemBaseInfusable
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.render.util.RGB
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemBindingEssence : ItemBaseInfusable(){
	init{
		maxStackSize = 16
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return true
	}
	
	override fun getSubItems(tab: CreativeTabs, items: NonNullList<ItemStack>){
		if (isInCreativeTab(tab)){
			items.add(ItemStack(this))
			
			for(infusion in Infusion.values()){
				items.add(ItemStack(this).also { InfusionTag.setList(it, InfusionList(infusion)) })
			}
		}
	}
	
	override fun getRarity(stack: ItemStack): EnumRarity{
		return EnumRarity.UNCOMMON
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
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
			.sortedWith(compareBy({ -it.value }, { Item.getIdFromItem(it.key) }))
		
		lines.add("")
		lines.add(I18n.translateToLocal("hee.infusions.applicable.title"))
		
		for((item, count) in applicableTo){
			lines.add(I18n.translateToLocalFormatted("hee.infusions.applicable.item", item.getItemStackDisplayName(ItemStack(item)), count))
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return false
	}
	
	@SideOnly(Side.CLIENT)
	object Color: IItemColor{
		private const val NONE = -1
		private val EMPTY = RGB(248u).toInt()
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int): Int{
			val list = InfusionTag.getList(stack).toList()
			
			if (list.isEmpty()){
				return EMPTY
			}
			
			return when(tintIndex){
				0 -> list[0].primaryColor
				1 -> list.getOrNull(1)?.primaryColor ?: list[0].secondaryColor
				2 -> list.getOrNull(2)?.primaryColor ?: list.getOrNull(1)?.secondaryColor ?: list[0].primaryColor
				3 -> list.getOrNull(3)?.primaryColor ?: list.getOrNull(2)?.primaryColor ?: (if (list.size == 2) list[0].secondaryColor else list[0].primaryColor)
				else -> NONE
			}
		}
	}
}
