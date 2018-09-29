package chylex.hee.game.particle
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.POWERED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleSetting
import chylex.hee.game.particle.util.ParticleSetting.ALL
import chylex.hee.game.particle.util.ParticleSetting.DECREASED
import chylex.hee.game.particle.util.ParticleSetting.MINIMAL
import chylex.hee.game.render.util.IColor
import chylex.hee.game.render.util.RGB
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.square
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

object ParticleEnergyCluster : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	// Particle data
	
	class Data(cluster: TileEntityEnergyCluster) : IParticleData{
		private companion object{
			private val COLOR_GRAY = adjustColorComponents(RGB(60))
			
			private fun adjustColorComponents(color: IColor): Int{
				val rgb = color.toRGB()
				return(rgb.red.coerceIn(64, 224) shl 16) or (rgb.green.coerceIn(64, 224) shl 8) or rgb.blue.coerceIn(64, 224)
			}
		}
		
		private val level = cluster.energyLevel.floating.value
		private val capacity = cluster.energyBaseCapacity.floating.value
		private val health = cluster.currentHealth
		
		private val colorPrimary = adjustColorComponents(cluster.color.primary(100F, 42F))
		private val colorSecondary = adjustColorComponents(cluster.color.secondary(90F, 42F))
		
		override fun generate(rand: Random): IntArray{
			val useSecondaryHue = when(health){
				REVITALIZING, UNSTABLE -> true
				POWERED                -> rand.nextBoolean()
				else                   -> rand.nextInt(4) == 0
			}
			
			val turnGray = useSecondaryHue && when(health){
				WEAKENED          -> rand.nextInt(100) < 25
				TIRED             -> rand.nextInt(100) < 75
				DAMAGED, UNSTABLE -> true
				else              -> false
			}
			
			val finalColor = when{
				turnGray        -> COLOR_GRAY
				useSecondaryHue -> colorSecondary
				else            -> colorPrimary
			}
			
			val finalScale = when(useSecondaryHue){
				true  -> (0.6F + (capacity * 0.07F) + (level * 0.008F)) * (if (health == POWERED) 1.6F else 1F)
				false ->  0.5F + (capacity * 0.03F) + (level * 0.06F)
			}
			
			return intArrayOf(
				finalColor,
				(finalScale * 100F).floorToInt()
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
	
	private const val FADE_IN_UNTIL = 6
	private const val FADE_OUT_AFTER = TOTAL_LIFESPAN - 10
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		private val clusterPos = Pos(posX, posY, posZ)
		
		private val alphaMultiplier = when(ParticleSetting.get(Minecraft.getMinecraft().gameSettings)){
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
				val color = unsafeData[0]
				val scale = unsafeData[1]
				
				particleRed = ((color shr 16) and 255) / 255F
				particleGreen = ((color shr 8) and 255) / 255F
				particleBlue = (color and 255) / 255F
				particleAlpha = 0F
				
				particleScale = scale * 0.01F
				
				particleMaxAge = TOTAL_LIFESPAN
				
				multiplyVelocity(square(particleScale * 0.5F).coerceAtMost(1F))
			}
		}
		
		override fun onUpdate(){
			if (clusterPos.getBlock(world) !== ModBlocks.ENERGY_CLUSTER){
				if (particleAge < FADE_OUT_AFTER){
					particleAge = FADE_OUT_AFTER
				}
				
				particleAge += 2
			}
			
			super.onUpdate()
			
			particleAlpha = alphaMultiplier * when{
				particleAge < FADE_IN_UNTIL  -> particleAge.toFloat() / FADE_IN_UNTIL
				particleAge > FADE_OUT_AFTER -> 1F - ((particleAge - FADE_OUT_AFTER).toFloat() / (TOTAL_LIFESPAN - FADE_OUT_AFTER))
				else                         -> 1F
			}
		}
	}
}
