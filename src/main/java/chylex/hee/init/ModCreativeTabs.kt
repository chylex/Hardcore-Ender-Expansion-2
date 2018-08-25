package chylex.hee.init
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ModCreativeTabs{
	lateinit var main: CreativeTabs
	
	fun initialize(){
		main = object: CreativeTabs("hee"){
			@SideOnly(Side.CLIENT)
			override fun getTabIconItem() = ItemStack(Blocks.AIR)
		}
	}
}
