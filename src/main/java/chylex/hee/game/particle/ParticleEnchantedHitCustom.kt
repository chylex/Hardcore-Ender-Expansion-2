package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseHit
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random

object ParticleEnchantedHitCustom : IParticleMaker.WithData<ParticleDataColorLifespanScale>() {
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data ?: DEFAULT_DATA.generate(rand))
	}
	
	fun Data(
		color: IRandomColor = DefaultColor,
		lifespan: IntRange = DEFAULT_LIFESPAN,
		scale: Float,
	) = ParticleDataColorLifespanScale.Generator(color, lifespan, scale..scale)
	
	fun Data(
		color: IntColor,
		scale: Float,
	) = ParticleDataColorLifespanScale.Generator(IRandomColor.Static(color), DEFAULT_LIFESPAN, scale..scale)
	
	private object DefaultColor : IRandomColor {
		override fun next(rand: Random): IntColor {
			return RGB(rand.nextInt(46, 69), rand.nextInt(122, 184), rand.nextInt(153, 230))
		}
	}
	
	private val DEFAULT_DATA = Data(scale = 1F)
	private val DEFAULT_LIFESPAN = (-1)..(-1)
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale) : ParticleBaseHit(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleEnchantedHitCustom.sprite)
			
			loadColor(data.color)
			particleScale = data.scale
			
			if (data.lifespan != -1) {
				maxAge = data.lifespan
			}
		}
	}
}
