package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.particle.util.ParticleTexture
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World

object ParticleFadingSpot : IParticleMaker<ParticleDataColorLifespanScale>{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	fun Data(
		color: IRandomColor,
		lifespan: IntRange,
		scale: ClosedFloatingPointRange<Float>
	) = ParticleDataColorLifespanScale.Generator(color, lifespan, scale)
	
	fun Data(
		color: IntColor,
		lifespan: IntRange,
		scale: ClosedFloatingPointRange<Float>
	) = ParticleDataColorLifespanScale.Generator(IRandomColor.Static(color), lifespan, scale)
	
	fun Data(
		color: IntColor,
		lifespan: Int,
		scale: Float
	) = ParticleDataColorLifespanScale.Generator(IRandomColor.Static(color), lifespan..lifespan, scale..scale)
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		private val alphaPerTick: Float
		private val scalePerTick: Float
		
		init{
			particleTexture = ParticleTexture.PIXEL
			
			if (data == null){
				alphaPerTick = 0F
				scalePerTick = 0F
				setExpired()
			}
			else{
				loadColor(data.color)
				particleScale = data.scale
				
				maxAge = data.lifespan
				
				alphaPerTick = 1F / maxAge
				scalePerTick = particleScale / (maxAge + rand.nextInt(1, 9))
			}
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
