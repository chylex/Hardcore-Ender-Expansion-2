package chylex.hee.client.model.entity
import chylex.hee.client.render.util.beginBox
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.toRadians
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.client.renderer.entity.model.RendererModel

@Sided(Side.CLIENT)
object ModelEntityBossEnderEye : EntityModel<EntityBossEnderEye>(){
	const val SCALE = 16F / 18F
	
	private val head: RendererModel
	private val eyes: Array<RendererModel>
	private val arms: RendererModel
	
	init{
		textureWidth = 128
		textureHeight = 64
		
		head = RendererModel(this).apply {
			setRotationPoint(0F, 15F, 0F)
			beginBox.offset(-9F, -9F, -9F).size(18, 18, 18).tex(0, 0).add()
		}
		
		eyes = Array(8){
			RendererModel(this).apply {
				setRotationPoint(0F, 15F, 0F)
				beginBox.offset(-8F, -8F, -9F).size(16, 16, 1).tex(-1 + (16 * it), 47).add()
				cubeList[0].let { it.quads = arrayOf(it.quads[4]) } // front face only
			}
		}
		
		arms = RendererModel(this).apply {
			setRotationPoint(0F, 15.5F, -0.5F)
			beginBox.offset(-12F, -1.5F, -1.5F).size(3, 27, 3).tex(73, 0).add()
			beginBox.offset(  9F, -1.5F, -1.5F).size(3, 27, 3).tex(73, 0).mirror().add()
		}
	}
	
	override fun setLivingAnimations(entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float){
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks)
		arms.rotateAngleX = (entity as? EntityBossEnderEye)?.clientArmAngle?.get(partialTicks)?.toRadians() ?: 0F
	}
	
	override fun setRotationAngles(entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float){
		super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)
		head.rotateAngleX = headPitch.toRadians()
	}
	
	override fun render(entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float){
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)
		
		val boss = entity as? EntityBossEnderEye
		
		val eyeState = when{
			boss == null -> 1
			boss.isSleeping -> 0
			else -> boss.demonLevel + 1
		}
		
		head.render(scale)
		eyes[eyeState].also { it.rotateAngleX = head.rotateAngleX }.render(scale)
		arms.render(scale)
	}
}
