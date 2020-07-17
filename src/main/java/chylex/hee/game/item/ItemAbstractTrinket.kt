package chylex.hee.game.item
import chylex.hee.client.util.MC
import chylex.hee.game.item.util.CustomRarity
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

open class ItemAbstractTrinket(properties: Properties) : Item(properties), ITrinketItem{
	companion object{
		fun onGetRarity(): Rarity{
			return CustomRarity.TRINKET
		}
		
		@Sided(Side.CLIENT)
		fun onAddInformation(stack: ItemStack, trinket: ITrinketItem, lines: MutableList<ITextComponent>){
			val player = MC.player ?: return
			
			if (lines.size > 1){ // first line is item name
				lines.add(StringTextComponent(""))
			}
			
			val keyInSlot = if (TrinketHandler.isInTrinketSlot(player, stack)) "in_slot" else "not_in_slot"
			val keyIsCharged = if (trinket.canPlaceIntoTrinketSlot(stack)) "charged" else "uncharged"
			
			lines.add(TranslationTextComponent("item.tooltip.hee.trinket.$keyInSlot.$keyIsCharged"))
		}
	}
	
	init{
		require(maxStackSize == 1){ "trinket item must have a maximum stack size of 1" }
	}
	
	override fun getRarity(stack: ItemStack): Rarity{
		return onGetRarity()
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		onAddInformation(stack, this, lines)
	}
}
