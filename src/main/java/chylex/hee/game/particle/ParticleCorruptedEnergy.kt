package chylex.hee.game.particle

import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.floorToInt
import chylex.hee.util.random.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import java.util.Random

object ParticleCorruptedEnergy : IParticleMaker.Simple() {
	private object Color : IColorGenerator {
		override fun next(rand: Random): IntColor {
			val color = ParticleTeleport.defaultColor.next(rand)
			
			if (rand.nextInt(3) != 0) {
				return color
			}
			
			val modified = color.asVec.mul(
				rand.nextFloat(0.0, 0.2),
				rand.nextFloat(0.0, 0.2),
				rand.nextFloat(0.1, 0.3)
			)
			
			return RGB((modified.x * 255.0).floorToInt(), (modified.y * 255.0).floorToInt(), (modified.z * 255.0).floorToInt())
		}
	}
	
	private val DATA = ParticleDataColorLifespanScale.Generator(Color, lifespan = 8..12, scale = (2.5F)..(5.0F))
	
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle {
		return ParticleTeleport.create(world, posX, posY, posZ, motX, motY, motZ, DATA.generate(rand))
	}
}
