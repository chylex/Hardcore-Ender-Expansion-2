package chylex.hee.game.item.base
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionTag
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class ItemBaseInfusable : Item(), IInfusableItem{
	companion object{
		fun onCanApplyInfusion(item: Item, infusion: Infusion): Boolean{
			return infusion.targetItems.contains(item)
		}
		
		fun onAddInformation(stack: ItemStack, lines: MutableList<String>){
			if (lines.size > 1){ // first line is item name
				lines.add("")
			}
			
			lines.add(I18n.translateToLocal("hee.infusions.title"))
			
			if (InfusionTag.hasAny(stack)){
				for(infusion in InfusionTag.getList(stack)){
					lines.add(I18n.translateToLocalFormatted("hee.infusions.item", I18n.translateToLocal(infusion.translationKey)))
				}
			}
			else{
				lines.add(I18n.translateToLocal("hee.infusions.none"))
			}
		}
		
		fun onHasEffect(stack: ItemStack): Boolean{
			return InfusionTag.hasAny(stack) // TODO use a custom milder and slower texture
		}
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return onCanApplyInfusion(this, infusion)
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, lines)
	}
	
	@SideOnly(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || onHasEffect(stack)
	}
}
