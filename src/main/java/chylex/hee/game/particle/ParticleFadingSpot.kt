package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleTexture
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random

object ParticleFadingSpot : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		private val color: IRandomColor,
		private val lifespan: IntRange = 0..0,
		private val scale: ClosedFloatingPointRange<Float> = 0F..0F
	) : IParticleData{
		constructor(color: IntColor, lifespan: IntRange = 0..0, scale: ClosedFloatingPointRange<Float> = 0F..0F) : this(IRandomColor.Static(color), lifespan, scale)
		
		override fun generate(rand: Random): IntArray{
			return intArrayOf(color.next(rand).i, rand.nextInt(lifespan), (rand.nextFloat(scale) * 100F).floorToInt())
		}
	}
	
	private val DEFAULT_DATA = IParticleData.Static(intArrayOf(
		RGB(0u).i, 0, 0
	))
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		private val alphaPerTick: Float
		private val scalePerTick: Float
		
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			
			particleTexture = ParticleTexture.PIXEL
			
			loadColor(data[0])
			
			particleScale = data[1] * 0.01F
			
			maxAge = data[2]
			
			alphaPerTick = 1F / maxAge
			scalePerTick = particleScale / (maxAge + rand.nextInt(1, 9))
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			particleAlpha -= alphaPerTick
			particleScale -= scalePerTick
		}
		
		override fun getFXLayer() = 1
		override fun setParticleTextureIndex(index: Int){}
	}
}
