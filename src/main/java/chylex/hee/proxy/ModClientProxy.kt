package chylex.hee.proxy
import chylex.hee.game.commands.HeeClientCommand
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
	}
}
