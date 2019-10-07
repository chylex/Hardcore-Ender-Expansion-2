package chylex.hee.game.particle
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleSetting
import chylex.hee.game.particle.util.ParticleSetting.ALL
import chylex.hee.game.particle.util.ParticleSetting.DECREASED
import chylex.hee.game.particle.util.ParticleSetting.MINIMAL
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.Pos
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.square
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY
import java.util.Random

object ParticleEnergyCluster : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	// Particle data
	
	class Data(private val cluster: TileEntityEnergyCluster) : IParticleData{
		override fun generate(rand: Random): IntArray{
			val data = cluster.particleDataGenerator?.next(rand) ?: return EMPTY_INT_ARRAY
			
			return intArrayOf(
				data.color,
				(data.scale * 100F).floorToInt()
			)
		}
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
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		private val clusterPos = Pos(posX, posY, posZ)
		
		private val alphaMultiplier = when(ParticleSetting.current){
			ALL       -> 0.32F
			DECREASED -> 0.46F
			MINIMAL   -> 0.58F
		}
		
		init{
			if (unsafeData.size < 2){
				particleAlpha = 0F
				particleMaxAge = 0
			}
			else{
				loadColor(unsafeData[0])
				particleAlpha = 0F
				
				particleScale = unsafeData[1] * 0.01F
				
				particleMaxAge = TOTAL_LIFESPAN
				
				multiplyVelocity(square(particleScale * 0.5F).coerceAtMost(1F))
			}
		}
		
		override fun onUpdate(){
			if (clusterPos.getBlock(world) !== ModBlocks.ENERGY_CLUSTER){
				if (particleAge < TOTAL_LIFESPAN - FADE_OUT_DURATION){
					particleAge = TOTAL_LIFESPAN - FADE_OUT_DURATION
				}
				
				particleAge += 2
			}
			
			super.onUpdate()
			particleAlpha = interpolateAge(alphaMultiplier, FADE_IN_DURATION, FADE_OUT_DURATION)
		}
	}
}
