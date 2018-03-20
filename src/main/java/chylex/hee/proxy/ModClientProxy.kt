package chylex.hee.proxy
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
}
