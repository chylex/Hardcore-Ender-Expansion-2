package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.floorToInt
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import java.util.Random

object ParticleTeleport : IParticleMaker.WithData<ParticleDataColorLifespanScale>(){
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data ?: DEFAULT_DATA.generate(rand))
	}
	
	fun Data(
		lifespan: IntRange = 35..50,
		scale: ClosedFloatingPointRange<Float> = (1.25F)..(1.45F)
	) = ParticleDataColorLifespanScale.Generator(DefaultColor, lifespan, scale)
	
	object DefaultColor : IRandomColor{
		override fun next(rand: Random): IntColor{
			val blue = rand.nextFloat(0.4F, 1.0F)
			val green = blue * 0.3F
			val red = blue * 0.9F
			
			return RGB((red * 255F).floorToInt(), (green * 255F).floorToInt(), (blue * 255F).floorToInt())
		}
	}
	
	private val DEFAULT_DATA = Data()
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			selectSpriteRandomly(ParticleTeleport.sprite)
			loadColor(data.color)
			particleScale = data.scale
			
			maxAge = data.lifespan
		}
		
		override fun getScale(partialTicks: Float): Float{
			return super.getScale(partialTicks) * (1F - (age + partialTicks) / (maxAge + 1F))
		}
	}
}
