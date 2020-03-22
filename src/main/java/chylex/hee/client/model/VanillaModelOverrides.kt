package chylex.hee.client.model
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.item.Items
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.resources.IResourceManager
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.resource.IResourceType
import net.minecraftforge.resource.ISelectiveResourceReloadListener
import net.minecraftforge.resource.VanillaResourceType
import java.util.function.Predicate

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object VanillaModelOverrides : ISelectiveResourceReloadListener{
	@SubscribeEvent
	fun onClientSetup(e: FMLClientSetupEvent){
		MC.instance.execute {
			(MC.instance.resourceManager as IReloadableResourceManager).addReloadListener(this)
			overrideModels()
		}
	}
	
	override fun onResourceManagerReload(resourceManager: IResourceManager, resourcePredicate: Predicate<IResourceType>){
		overrideModels()
	}
	
	override fun getResourceType() = VanillaResourceType.MODELS
	
	// Overrides
	
	private val BREWING_STAND_TEX = Resource.Custom("item/brewing_stand")
	private val BREWING_STAND_MODEL = ModelResourceLocation(Resource.Custom("brewing_stand"), "inventory")
	
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		if (e.map.basePath != "textures"){
			return
		}
		
		with(e){
			addSprite(BREWING_STAND_TEX)
		}
	}
	
	@SubscribeEvent
	fun onRegisterModels(@Suppress("UNUSED_PARAMETER") e: ModelRegistryEvent){
		ModelLoader.addSpecialModel(BREWING_STAND_MODEL)
	}
	
	private fun overrideModels(){
		with(MC.itemRenderer.itemModelMesher){
			register(Items.BREWING_STAND, BREWING_STAND_MODEL)
		}
	}
}
