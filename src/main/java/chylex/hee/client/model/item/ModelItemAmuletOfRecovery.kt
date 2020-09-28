package chylex.hee.client.model.item
import chylex.hee.HEE
import chylex.hee.client.MC
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.BakedModelWrapper
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@Sided(Side.CLIENT)
@Suppress("unused")
class ModelItemAmuletOfRecovery private constructor(sourceModel: IBakedModel) : BakedModelWrapper<IBakedModel>(sourceModel){
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	companion object{
		private val RESOURCE_NORMAL = ModelResourceLocation(Resource.Custom("amulet_of_recovery"), "inventory")
		private val RESOURCE_HELD   = Resource.Custom("item/amulet_of_recovery_held")
		
		private lateinit var modelRegistry: MutableMap<ResourceLocation, IBakedModel>
		
		@SubscribeEvent
		fun onRegisterModels(@Suppress("UNUSED_PARAMETER") e: ModelRegistryEvent){
			ModelLoader.addSpecialModel(RESOURCE_HELD)
		}
		
		@SubscribeEvent
		fun onModelBake(e: ModelBakeEvent){
			modelRegistry = e.modelRegistry
			modelRegistry[RESOURCE_NORMAL] = ModelItemAmuletOfRecovery(modelRegistry.getValue(RESOURCE_NORMAL))
		}
	}
	
	override fun handlePerspective(transformType: TransformType, matrix: MatrixStack): IBakedModel = when(transformType){
		FIRST_PERSON_LEFT_HAND,
		FIRST_PERSON_RIGHT_HAND,
		THIRD_PERSON_LEFT_HAND,
		THIRD_PERSON_RIGHT_HAND ->
			modelRegistry.getOrElse(RESOURCE_HELD){ MC.instance.modelManager.missingModel }.handlePerspective(transformType, matrix)
		
		else ->
			super.handlePerspective(transformType, matrix)
	}
}
