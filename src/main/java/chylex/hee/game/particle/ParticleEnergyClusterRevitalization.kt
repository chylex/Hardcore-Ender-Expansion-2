package chylex.hee.game.particle
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetTowards
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleEnergyClusterRevitalization : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	private const val BASE_ALPHA = 0.65F
	
	const val TOTAL_LIFESPAN = 60
	private const val FADE_IN_DURATION = 4
	private const val FADE_OUT_DURATION = 15
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		private val clusterPos = Pos(posX, posY, posZ)
		
		private val isRevitalizing: Boolean
		private val targetDistance: Double
		private var motionTarget = rand.nextVector(0.02)
		private var motionOffset = rand.nextVector(1.0)
		
		init{
			particleRed = 1F
			particleGreen = 1F
			particleBlue = 1F
			particleAlpha = 0F
			
			particleMaxAge = TOTAL_LIFESPAN
			
			val cluster = clusterPos.getTile<TileEntityEnergyCluster>(world)
			
			if (cluster == null){
				particleAge = particleMaxAge
				
				isRevitalizing = false
				targetDistance = 0.0
			}
			else{
				val energyNormalized = cluster.energyLevel.floating.value / IEnergyQuantity.MAX_REGEN_CAPACITY.floating.value
				
				particleScale = 0.28F + (energyNormalized * 0.17F)
				
				isRevitalizing = cluster.currentHealth == REVITALIZING
				targetDistance = rand.nextFloat(0.18, 0.2) + (energyNormalized * 0.42)
			}
		}
		
		override fun onUpdate(){
			if ((clusterPos.getTile<TileEntityEnergyCluster>(world)?.clientOrbitingOrbs ?: 0) == 0.toByte()){
				if (particleAge < TOTAL_LIFESPAN - FADE_OUT_DURATION){
					particleAge = TOTAL_LIFESPAN - FADE_OUT_DURATION
				}
				
				particleAge += 3
			}
			
			super.onUpdate()
			particleAlpha = interpolateAge(BASE_ALPHA, FADE_IN_DURATION, FADE_OUT_DURATION)
			
			val posVec = Vec3d(posX, posY, posZ)
			val newPos: Vec3d
			
			if (isRevitalizing && particleAge > TOTAL_LIFESPAN - FADE_OUT_DURATION){
				newPos = posVec.offsetTowards(clusterPos.center, 1.0 - (TOTAL_LIFESPAN - particleAge).toDouble() / TOTAL_LIFESPAN)
			}
			else{
				motionTarget = motionTarget.offsetTowards(motionOffset, 0.1).normalize()
				motionOffset = motionOffset.add(rand.nextVector(if (isRevitalizing) 0.04 else 0.01)).normalize()
				
				newPos = clusterPos.center.add(motionTarget.scale(targetDistance))
			}
			
			setPosition(newPos.x, newPos.y, newPos.z)
		}
	}
}
