package chylex.hee.client.model.item
import chylex.hee.client.util.MC
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
import net.minecraft.client.renderer.block.model.ModelBakery
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.model.BakedModelWrapper
import net.minecraftforge.common.MinecraftForge
import org.apache.commons.lang3.tuple.Pair
import javax.vecmath.Matrix4f

@Sided(Side.CLIENT)
class ModelItemAmuletOfRecovery private constructor(sourceModel: IBakedModel) : BakedModelWrapper<IBakedModel>(sourceModel){
	companion object{
		private val RESOURCE_NORMAL = ModelResourceLocation(Resource.Custom("amulet_of_recovery"), "inventory")
		private val RESOURCE_HELD   = ModelResourceLocation(Resource.Custom("amulet_of_recovery_held"), "held")
		
		fun register(){
			ModelBakery.registerItemVariants(ModItems.AMULET_OF_RECOVERY, RESOURCE_NORMAL, RESOURCE_HELD)
			MinecraftForge.EVENT_BUS.register(this)
		}
		
		@SubscribeEvent
		fun onModelBake(e: ModelBakeEvent){
			with(e.modelRegistry){
				putObject(RESOURCE_NORMAL, ModelItemAmuletOfRecovery(getObject(RESOURCE_NORMAL)!!))
			}
		}
	}
	
	override fun handlePerspective(transformType: TransformType): Pair<out IBakedModel, Matrix4f> = when(transformType){
		FIRST_PERSON_LEFT_HAND,
		FIRST_PERSON_RIGHT_HAND,
		THIRD_PERSON_LEFT_HAND,
		THIRD_PERSON_RIGHT_HAND ->
			MC.itemRenderer.itemModelMesher.modelManager.getModel(RESOURCE_HELD).handlePerspective(transformType)
		
		else ->
			super.handlePerspective(transformType)
	}
}
