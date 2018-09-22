package chylex.hee.proxy
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.render.block.RenderTileEndPortal
import chylex.hee.game.render.entity.RenderEntityItemNoBob
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.factory.RendererConstructors
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused", "RemoveExplicitTypeArguments")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onPreInit(){
		MinecraftForge.EVENT_BUS.register(this)
		
		registerEntityRenderer<EntityItemIgneousRock, RenderEntityItemNoBob>()
	}
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
		
		registerTileRenderer<TileEntityEndPortal>(RenderTileEndPortal)
		
		val blockColors = Minecraft.getMinecraft().blockColors
		val itemColors = Minecraft.getMinecraft().itemColors
		
		with(blockColors){
			registerBlockColorHandler(BlockDryVines.Color, ModBlocks.DRY_VINES)
		}
		
		with(itemColors){
			registerItemColorHandler(IItemColor {
				stack, tintIndex -> blockColors.colorMultiplier((stack.item as ItemBlock).block.getStateFromMeta(stack.metadata), null, null, tintIndex) // UPDATE
			}, ModBlocks.DRY_VINES)
			
			registerItemColorHandler(ItemEnergyOracle.Color, ModItems.ENERGY_ORACLE)
			registerItemColorHandler(ItemEnergyReceptacle.Color, ModItems.ENERGY_RECEPTACLE)
		}
	}
	
	@SubscribeEvent
	fun onModels(e: ModelRegistryEvent){
		with(ForgeRegistries.ITEMS){
			for(item in keys.asSequence().filter { it.resourceDomain == HardcoreEnderExpansion.ID }.map(::getValue)){
				ModelLoader.setCustomModelResourceLocation(item!!, 0, ModelResourceLocation(item.registryName!!, "inventory"))
			}
		}
	}
	
	private inline fun <reified T : Entity, reified R : Render<in T>> registerEntityRenderer(){
		RenderingRegistry.registerEntityRenderingHandler(T::class.java, RendererConstructors.get(R::class.java))
	}
	
	private inline fun <reified T : TileEntity> registerTileRenderer(renderer: TileEntitySpecialRenderer<in T>){
		ClientRegistry.bindTileEntitySpecialRenderer(T::class.java, renderer)
	}
}
