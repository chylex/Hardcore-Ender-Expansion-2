package chylex.hee.proxy
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.init.ModItems
import chylex.hee.render.block.RenderTileEndPortal
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
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
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEndPortal::class.java, RenderTileEndPortal)
		
		with(Minecraft.getMinecraft().itemColors){
			registerItemColorHandler(ItemEnergyReceptacle.Color, ModItems.ENERGY_RECEPTACLE)
		}
	}
	
	@SubscribeEvent
	fun onModels(e: ModelRegistryEvent){
		with(ForgeRegistries.ITEMS){
			for(item in keys.filter { it.resourceDomain == HardcoreEnderExpansion.ID }.map(::getValue)){
				ModelLoader.setCustomModelResourceLocation(item!!, 0, ModelResourceLocation(item.registryName!!, "inventory"))
			}
		}
	}
}
