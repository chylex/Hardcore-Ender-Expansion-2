package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.color.IColor
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

object ParticleGlitter : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	abstract class Data(val maxAgeMultiplier: IntRange) : IParticleData{
		protected abstract fun nextColor(rand: Random): IColor
		
		final override fun generate(rand: Random): IntArray{
			return intArrayOf(nextColor(rand).toInt(), maxAgeMultiplier.start, maxAgeMultiplier.endInclusive)
		}
	}
	
	private val DEFAULT_DATA = IParticleData.Static(intArrayOf(
		RGB(0u).toInt(), 0, 0
	))
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			
			particleTextureIndexX = rand.nextInt(1, 4)
			particleTextureIndexY = 0
			
			loadColor(data[0])
			particleAlpha = rand.nextFloat(0.1F, 1F)
			
			particleScale = rand.nextFloat(0.35F, 0.5F)
			
			particleMaxAge = (4F / rand.nextFloat(0.1F, 1F)).toInt() * rand.nextInt(data[1], data[2])
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			if (particleAge < (particleMaxAge * 3) / 4 && rand.nextInt(5) == 0){
				particleAlpha = rand.nextFloat(0.5F, 1F)
			}
			
			particleAlpha -= 0.025F
		}
	}
}
