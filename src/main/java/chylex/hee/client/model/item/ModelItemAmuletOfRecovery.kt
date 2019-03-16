package chylex.hee.client.model.item
import chylex.hee.init.ModItems
import chylex.hee.system.Resource.Custom
import net.minecraft.client.Minecraft
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.commons.lang3.tuple.Pair
import javax.vecmath.Matrix4f

@SideOnly(Side.CLIENT)
class ModelItemAmuletOfRecovery private constructor(sourceModel: IBakedModel) : BakedModelWrapper<IBakedModel>(sourceModel){
	companion object{
		private val RESOURCE_NORMAL = ModelResourceLocation(Custom("amulet_of_recovery"), "inventory")
		private val RESOURCE_HELD   = ModelResourceLocation(Custom("amulet_of_recovery_held"), "held")
		
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
			Minecraft.getMinecraft().renderItem.itemModelMesher.modelManager.getModel(RESOURCE_HELD).handlePerspective(transformType)
		
		else ->
			super.handlePerspective(transformType)
	}
}
