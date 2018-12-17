package chylex.hee.game.item.base
import chylex.hee.HEE
import chylex.hee.game.item.trinket.ITrinketItem
import chylex.hee.game.item.util.CustomRarity
import chylex.hee.game.mechanics.TrinketHandler
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

open class ItemBaseTrinket : Item(), ITrinketItem{
	companion object{
		fun onGetRarity(): EnumRarity{
			return CustomRarity.TRINKET
		}
		
		fun onAddInformation(stack: ItemStack, trinket: ITrinketItem, lines: MutableList<String>){
			val player = HEE.proxy.getClientSidePlayer() ?: return
			
			if (lines.size > 1){ // first line is item name
				lines.add("")
			}
			
			val keyInSlot = if (TrinketHandler.isInTrinketSlot(player, stack)) "in_slot" else "not_in_slot"
			val keyIsCharged = if (trinket.canPlaceIntoTrinketSlot(stack)) "charged" else "uncharged"
			
			lines.add(I18n.translateToLocal("item.tooltip.hee.trinket.$keyInSlot.$keyIsCharged"))
		}
	}
	
	init{
		maxStackSize = 1
	}
	
	override fun getRarity(stack: ItemStack): EnumRarity{
		return onGetRarity()
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, this, lines)
	}
}
