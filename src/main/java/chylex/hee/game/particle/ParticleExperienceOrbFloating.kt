package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBase
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec3
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

object ParticleExperienceOrbFloating : IParticleMaker.WithData<ParticleDataColorLifespanScale>(){
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return Instance(world, posX, posY, posZ, motY, data ?: DEFAULT_DATA.generate(rand))
	}
	
	fun Data(
		lifespan: Int
	) = ParticleDataColorLifespanScale.Generator(DEFAULT_COLOR, lifespan..lifespan, 1F..1F)
	
	val DEFAULT_COLOR = IRandomColor { RGB(nextInt(0, 255), 255, nextInt(0, 51)) }
	
	private val DEFAULT_DATA = Data(lifespan = 100)
	
	@Sided(Side.CLIENT)
	class Instance(world: World, posX: Double, posY: Double, posZ: Double, motY: Double, data: ParticleDataColorLifespanScale) : ParticleBase(world, posX, posY, posZ, 0.0, 0.0, 0.0){
		private val motionOffset: Double
		
		init{
			selectSpriteRandomly(ParticleExperienceOrbFloating.sprite)
			
			loadColor(data.color)
			particleScale = data.scale
			
			maxAge = data.lifespan
			
			motionVec = Vec3.y(motY)
			motionOffset = rand.nextFloat(-PI, PI)
		}
		
		override fun tick(){
			super.tick()
			
			motionX = sin(motionOffset + sign(motionOffset) * (age / 8.0)) * 0.02
			motionZ = cos(motionOffset + sign(motionOffset) * (age / 8.0)) * 0.02
			
			if (age > maxAge - 10){
				particleAlpha -= 0.1F
			}
		}
	}
}
