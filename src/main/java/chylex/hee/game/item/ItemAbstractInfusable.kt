package chylex.hee.game.item
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentString
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

abstract class ItemAbstractInfusable(properties: Properties) : Item(properties), IInfusableItem{
	companion object{
		fun onCanApplyInfusion(item: Item, infusion: Infusion): Boolean{
			return infusion.targetItems.contains(item)
		}
		
		@Sided(Side.CLIENT)
		fun onAddInformation(stack: ItemStack, lines: MutableList<ITextComponent>){
			if (lines.size > 1){ // first line is item name
				lines.add(TextComponentString(""))
			}
			
			lines.add(TextComponentTranslation("hee.infusions.list.title"))
			
			if (InfusionTag.hasAny(stack)){
				for(infusion in InfusionTag.getList(stack)){
					lines.add(TextComponentTranslation("hee.infusions.list.item", TextComponentTranslation(infusion.translationKey)))
				}
			}
			else{
				lines.add(TextComponentTranslation("hee.infusions.list.none"))
			}
		}
		
		fun onHasEffect(stack: ItemStack): Boolean{
			return InfusionTag.hasAny(stack) // TODO use a custom milder and slower texture
		}
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return onCanApplyInfusion(this, infusion)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || onHasEffect(stack)
	}
}
