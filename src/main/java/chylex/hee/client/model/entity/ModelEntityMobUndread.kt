package chylex.hee.client.model.entity

import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.toRadians
import net.minecraft.client.renderer.entity.model.AbstractZombieModel

@Sided(Side.CLIENT)
class ModelEntityMobUndread private constructor(modelSize: Float, textureWidth: Int, textureHeight: Int) : AbstractZombieModel<EntityMobUndread>(modelSize, 0F, textureWidth, textureHeight) {
	constructor(modelSize: Float, tallTexture: Boolean) : this(modelSize, 64, if (tallTexture) 32 else 64)
	constructor() : this(0F, false)
	
	override fun setRotationAngles(entity: EntityMobUndread, limbSwing: Float, limbSwingAmount: Float, age: Float, headYaw: Float, headPitch: Float) {
		super.setRotationAngles(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch)
		
		val armAngle = if (isAggressive(entity)) 273F.toRadians() else 279F.toRadians()
		bipedLeftArm.rotateAngleX = armAngle
		bipedRightArm.rotateAngleX = armAngle
	}
	
	override fun isAggressive(entity: EntityMobUndread): Boolean {
		return entity.isAggressive
	}
}
