package chylex.hee.game.particle
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.Pos
import chylex.hee.game.world.center
import chylex.hee.game.world.getTile
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextVector
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ParticleEnergyClusterRevitalization : IParticleMaker.Simple(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	private const val BASE_ALPHA = 0.65F
	
	const val TOTAL_LIFESPAN = 60
	private const val FADE_IN_DURATION = 4
	private const val FADE_OUT_DURATION = 15
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		private val clusterPos = Pos(posX, posY, posZ)
		
		private val isRevitalizing: Boolean
		private val targetDistance: Double
		private var motionTarget = rand.nextVector(0.02)
		private var motionOffset = rand.nextVector(1.0)
		
		init{
			selectSpriteRandomly(ParticleEnergyClusterRevitalization.sprite)
			loadColor(RGB(255u))
			particleAlpha = 0F
			
			maxAge = TOTAL_LIFESPAN
			
			val cluster = clusterPos.getTile<TileEntityEnergyCluster>(world)
			
			if (cluster == null){
				age = maxAge
				
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
		
		override fun tick(){
			if ((clusterPos.getTile<TileEntityEnergyCluster>(world)?.clientOrbitingOrbs ?: 0) == 0.toByte()){
				if (age < TOTAL_LIFESPAN - FADE_OUT_DURATION){
					age = TOTAL_LIFESPAN - FADE_OUT_DURATION
				}
				
				age += 3
			}
			
			super.tick()
			particleAlpha = interpolateAge(BASE_ALPHA, FADE_IN_DURATION, FADE_OUT_DURATION)
			
			val posVec = Vec(posX, posY, posZ)
			val newPos: Vec3d
			
			if (isRevitalizing && age > TOTAL_LIFESPAN - FADE_OUT_DURATION){
				newPos = posVec.offsetTowards(clusterPos.center, 1.0 - (TOTAL_LIFESPAN - age).toDouble() / TOTAL_LIFESPAN)
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
