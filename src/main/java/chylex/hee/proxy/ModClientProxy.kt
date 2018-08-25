package chylex.hee.proxy
import chylex.hee.game.commands.HeeClientCommand
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onPreInit(){
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
	}
	
	@SubscribeEvent
	fun onModels(e: ModelRegistryEvent){
		fun register(vararg items: Item){
			for(item in items){
				ModelLoader.setCustomModelResourceLocation(item, 0, ModelResourceLocation(item.registryName!!, "inventory"))
			}
		}
		
		register(
		)
	}
}
