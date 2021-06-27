package chylex.hee.game.particle

import chylex.hee.game.entity.lookDirVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.particle.ParticleDust.Data
import chylex.hee.game.particle.base.ParticleBase
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.Pos
import chylex.hee.game.world.isLoaded
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.square
import chylex.hee.system.math.toRadians
import chylex.hee.system.math.withY
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextVector
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object ParticleDust : IParticleMaker.WithData<Data>() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		val lifespan: IntRange,
		val scale: ClosedFloatingPointRange<Float>,
		val reactsToSkyLight: Boolean,
	) : IParticleData.Self<Data>()
	
	private const val COLLISION_SIZE = 1.9
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?) : ParticleBase(world, posX, posY, posZ, motX, motY, motZ) {
		private val preSkyLightColor: IntColor?
		private var chaosVec = Vec3.ZERO
		
		init {
			selectSpriteRandomly(ParticleDust.sprite)
			particleAlpha = 0F
			
			if (data == null) {
				preSkyLightColor = null
				setExpired()
			}
			else {
				val color = RGB(rand.nextInt(90, 100), rand.nextInt(80, 90), 5)
				loadColor(color)
				preSkyLightColor = color.takeIf { data.reactsToSkyLight }
				particleScale = rand.nextFloat(data.scale)
				
				particleGravity = rand.nextFloat(0.0014F, 0.0032F)
				motionVec = rand.nextVector(0.005)
				
				maxAge = rand.nextInt(data.lifespan)
			}
		}
		
		override fun tick() {
			super.tick()
			
			if (age < 35) {
				particleAlpha = min(0.5F, particleAlpha + 0.015F)
			}
			else if (age > maxAge - 35) {
				particleAlpha = max(0F, particleAlpha - 0.015F)
			}
			
			val aabb = AxisAlignedBB(
				posX - COLLISION_SIZE,
				posY - COLLISION_SIZE,
				posZ - COLLISION_SIZE,
				posX + COLLISION_SIZE,
				posY + COLLISION_SIZE,
				posZ + COLLISION_SIZE
			)
			
			for(entity in world.selectExistingEntities.allInBox(aabb)) {
				var mot = entity.motion
				
				if (mot.y < 0.0 && entity.isOnGround) {
					mot = mot.withY(0.0)
				}
				
				val lenSq = mot.lengthSquared()
				
				if (lenSq > square(0.01)) {
					val len = sqrt(lenSq).coerceAtMost(2.5)
					
					chaosVec = chaosVec
						.add(mot.normalize().scale(-0.08 - (0.06 * len)))
						.add(rand.nextVector(0.05 * len))
				}
				
				if (entity is EntityLivingBase && entity.isSwingInProgress) {
					val strength = if (entity.getHeldItem(entity.swingingHand).isEmpty) 0.07 else 0.13
					
					chaosVec = chaosVec
						.add(entity.lookDirVec.scale(-strength))
						.add(rand.nextVector(strength * 3))
				}
			}
			
			if (motionVec.lengthSquared() > square(0.01)) {
				motionVec = motionVec.scale(0.92)
			}
			
			if (chaosVec.lengthSquared() > square(0.01)) {
				val f1 = cos((age * 7.0).toRadians()) + 0.7
				val f2 = sin((age * 3.7).toRadians())
				motionVec = motionVec.add(chaosVec.scale(f1 * 0.03)).add(rand.nextVector(f2 * 0.01))
				chaosVec = chaosVec.scale(0.88)
				++age
			}
			
			val preSkyLightColor = preSkyLightColor
			
			if (preSkyLightColor != null) {
				val pos = Pos(posX, posY, posZ)
				
				if (pos.isLoaded(world)) {
					val sky = world.getLightFor(SKY, pos) / 16F
					particleRed = (preSkyLightColor.redF + (sky * 0.25F)).coerceIn(0F, 1F)
					particleGreen = (preSkyLightColor.greenF - (sky * 0.1F)).coerceIn(0F, 1F)
					particleBlue = (preSkyLightColor.blueF + (sky * 0.55F)).coerceIn(0F, 1F)
				}
			}
		}
		
		override fun getBrightnessForRender(partialTick: Float): Int {
			val pos = Pos(posX, posY, posZ)
			
			if (!pos.isLoaded(world)) {
				return 0
			}
			
			val sky = world.getLightFor(SKY, pos).coerceAtLeast(4)
			val block = world.getLightFor(BLOCK, pos).coerceAtLeast(4)
			
			return (sky shl 20) or (block shl 4)
		}
		
		override fun getRenderType(): IParticleRenderType {
			return PARTICLE_SHEET_TRANSLUCENT
		}
	}
}
