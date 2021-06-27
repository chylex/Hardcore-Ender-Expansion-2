package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextFloat
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import kotlin.math.min

object ParticleGrowingSpot : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	override val defaultColor = IRandomColor.Static(RGB(0u))
	override val defaultLifespan = 0..0
	override val defaultScale = 1F..1F
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleGrowingSpot.sprite)
			
			if (data == null) {
				setExpired()
			}
			else {
				loadColor(data.color)
				particleAlpha = 0.25F
				particleScale = rand.nextFloat(0.25F, 0.35F) * data.scale
				
				maxAge = data.lifespan
			}
		}
		
		override fun tick() {
			super.tick()
			
			particleAlpha = min(0.9F, particleAlpha + rand.nextFloat(0.03F, 0.09F))
			particleScale += rand.nextFloat(0.01F, 0.02F)
		}
		
		override fun getRenderType(): IParticleRenderType {
			return PARTICLE_SHEET_TRANSLUCENT
		}
	}
}
