package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import kotlin.math.min

object ParticleGrowingSpot : IParticleMaker.WithData<ParticleDataColorLifespanScale>(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	fun Data(
		color: IntColor,
		lifespan: Int
	) = ParticleDataColorLifespanScale.Generator(IRandomColor.Static(color), lifespan..lifespan, 1F..1F)
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			selectSpriteRandomly(ParticleGrowingSpot.sprite)
			
			if (data == null){
				setExpired()
			}
			else{
				loadColor(data.color)
				particleAlpha = 0.25F
				particleScale = rand.nextFloat(0.25F, 0.35F) * data.scale
				
				maxAge = data.lifespan
			}
		}
		
		override fun tick(){
			super.tick()
			
			particleAlpha = min(0.9F, particleAlpha + rand.nextFloat(0.03F, 0.09F))
			particleScale += rand.nextFloat(0.01F, 0.02F)
		}
	}
}
