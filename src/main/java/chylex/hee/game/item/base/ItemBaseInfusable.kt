package chylex.hee.game.item.base
import chylex.hee.game.item.infusion.InfusionTag
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class ItemBaseInfusable : Item(){
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		
		if (lines.isNotEmpty()){
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
	
	@SideOnly(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || InfusionTag.hasAny(stack) // TODO use a custom milder and slower texture
	}
}
