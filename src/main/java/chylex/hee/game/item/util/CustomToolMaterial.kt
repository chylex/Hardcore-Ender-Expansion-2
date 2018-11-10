package chylex.hee.game.item.util
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.init.ModItems
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.EnumHelper

object CustomToolMaterial{
	val VOID = EnumHelper.addToolMaterial("HEE_VOID", IRON, 925, 15F, 0F, 1)!!.also { it.setRepairItem(ItemStack(ModItems.VOID_ESSENCE)) }
}
