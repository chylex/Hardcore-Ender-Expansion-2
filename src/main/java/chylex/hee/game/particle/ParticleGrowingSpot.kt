package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleTexture
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import kotlin.math.min

object ParticleGrowingSpot : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		color: IntColor = RGB(0u),
		lifespan: Int = 0
	) : IParticleData.Static(intArrayOf(
		color.i,
		lifespan
	))
	
	private val DEFAULT_DATA = Data()
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			
			particleTexture = ParticleTexture.PIXEL
			
			loadColor(data[0])
			particleAlpha = 0.25F
			
			particleScale = rand.nextFloat(0.25F, 0.35F)
			
			particleMaxAge = data[1]
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			particleAlpha = min(0.9F, particleAlpha + rand.nextFloat(0.03F, 0.09F))
			particleScale += rand.nextFloat(0.01F, 0.02F)
		}
		
		override fun getFXLayer() = 1
		override fun setParticleTextureIndex(index: Int){}
	}
}
