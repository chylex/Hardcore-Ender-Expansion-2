package chylex.hee.game.item.util
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.init.ModItems
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.EnumHelper

object CustomToolMaterial{
	val VOID = EnumHelper.addToolMaterial("HEE_VOID", IRON, 925, 15F, 0F, 1)!!
	val SCORCHING_TOOL = EnumHelper.addToolMaterial("HEE_SCORCHING_TOOL", DIAMOND, 175, 10F, 2F, 0)!!
	val SCORCHING_SWORD = EnumHelper.addToolMaterial("HEE_SCORCHING_SWORD", DIAMOND, 275, 10F, 5F, 0)!! // UPDATE check if hand+sword still add 1+3 damage
	
	fun setupRepairItems(){
		VOID.setRepairItem(ItemStack(ModItems.VOID_ESSENCE))
		SCORCHING_TOOL.setRepairItem(ItemStack(ModItems.INFERNIUM_INGOT))
		SCORCHING_SWORD.setRepairItem(ItemStack(ModItems.INFERNIUM_INGOT))
	}
}
