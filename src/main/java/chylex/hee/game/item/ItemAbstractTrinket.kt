package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.game.item.util.CustomRarity
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

open class ItemAbstractTrinket : Item(), ITrinketItem{
	companion object{
		fun onGetRarity(): EnumRarity{
			return CustomRarity.TRINKET
		}
		
		@Sided(Side.CLIENT)
		fun onAddInformation(stack: ItemStack, trinket: ITrinketItem, lines: MutableList<String>){
			val player = HEE.proxy.getClientSidePlayer() ?: return
			
			if (lines.size > 1){ // first line is item name
				lines.add("")
			}
			
			val keyInSlot = if (TrinketHandler.isInTrinketSlot(player, stack)) "in_slot" else "not_in_slot"
			val keyIsCharged = if (trinket.canPlaceIntoTrinketSlot(stack)) "charged" else "uncharged"
			
			lines.add(I18n.format("item.tooltip.hee.trinket.$keyInSlot.$keyIsCharged"))
		}
	}
	
	init{
		maxStackSize = 1
	}
	
	override fun getRarity(stack: ItemStack): EnumRarity{
		return onGetRarity()
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, this, lines)
	}
}
