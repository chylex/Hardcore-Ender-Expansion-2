package chylex.hee.game.particle
import chylex.hee.client.MC
import chylex.hee.game.particle.ParticleSetting.ALL
import chylex.hee.game.particle.ParticleSetting.DECREASED
import chylex.hee.game.particle.ParticleSetting.MINIMAL
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.square
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random

object ParticleEnergyCluster : IParticleMaker.WithData<ParticleDataColorScale>(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	// Skip test
	
	private const val MAX_DISTANCE = 160.0
	private const val MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE
	
	private const val LQ_DISTANCE = 40.0
	private const val LQ_DISTANCE_SQ = LQ_DISTANCE * LQ_DISTANCE
	
	fun newCountingSkipTest(): (Double, ParticleSetting, Random) -> Boolean{
		var counter = 0
		
		return { distanceSq, particleSetting, _ ->
			++counter
			
			(distanceSq > MAX_DISTANCE_SQ) || (distanceSq > LQ_DISTANCE_SQ && counter % 4 == 0) || when(particleSetting){
				ALL       -> false
				DECREASED -> counter % 3 == 0
				MINIMAL   -> counter % 2 == 0
			}
		}
	}
	
	// Particle instance
	
	private const val TOTAL_LIFESPAN = 25
	private const val FADE_IN_DURATION = 6
	private const val FADE_OUT_DURATION = 10
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		private val clusterPos = Pos(posX, posY, posZ)
		
		private val alphaMultiplier = when(MC.particleSetting){
			ALL       -> 0.32F
			DECREASED -> 0.46F
			MINIMAL   -> 0.58F
		}
		
		init{
			selectSpriteRandomly(ParticleEnergyCluster.sprite)
			
			if (data == null){
				setExpired()
			}
			else{
				loadColor(data.color)
				particleAlpha = 0F
				particleScale = data.scale
				
				maxAge = TOTAL_LIFESPAN
				
				multiplyVelocity(square(particleScale * 0.5F).coerceAtMost(1F))
			}
		}
		
		override fun tick(){
			if (clusterPos.getBlock(world) !== ModBlocks.ENERGY_CLUSTER){
				if (age < TOTAL_LIFESPAN - FADE_OUT_DURATION){
					age = TOTAL_LIFESPAN - FADE_OUT_DURATION
				}
				
				age += 2
			}
			
			super.tick()
			particleAlpha = interpolateAge(alphaMultiplier, FADE_IN_DURATION, FADE_OUT_DURATION)
		}
	}
}
