package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random

object ParticleGlitter : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		private val color: IRandomColor,
		private val maxAgeMultiplier: IntRange
	) : IParticleData{
		override fun generate(rand: Random): IntArray{
			return intArrayOf(color.next(rand).i, maxAgeMultiplier.first, maxAgeMultiplier.last)
		}
	}
	
	private val DEFAULT_DATA = IParticleData.Static(intArrayOf(
		RGB(0u).i, 0, 0
	))
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			
			particleTextureIndexX = rand.nextInt(1, 4)
			particleTextureIndexY = 0
			
			loadColor(data[0])
			particleAlpha = rand.nextFloat(0.1F, 1F)
			
			particleScale = rand.nextFloat(0.35F, 0.5F)
			
			maxAge = (4F / rand.nextFloat(0.1F, 1F)).toInt() * rand.nextInt(data[1], data[2])
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			if (age < (maxAge * 3) / 4 && rand.nextInt(5) == 0){
				particleAlpha = rand.nextFloat(0.5F, 1F)
			}
			
			particleAlpha -= 0.025F
		}
	}
}
