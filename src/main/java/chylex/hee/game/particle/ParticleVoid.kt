package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec
import chylex.hee.system.math.addY
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.scale
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextVector
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

object ParticleVoid : IParticleMaker.Simple(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			selectSpriteRandomly(ParticleVoid.sprite)
			
			val color = rand.nextFloat(0.25F, 0.35F)
			val motMp = rand.nextFloat(1F, 3F) * 0.001F
			
			particleRed = color
			particleGreen = color
			particleBlue = color
			particleAlpha = 0F
			
			particleScale = rand.nextFloat(0.25F, 0.4F)
			
			maxAge = (30F / rand.nextFloat(0.3F, 1F)).ceilToInt()
			
			motionVec = Vec(
				rand.nextFloat(-1.0, 1.0),
				rand.nextFloat(-1.0, 1.0),
				rand.nextFloat(-1.0, 1.0)
			).normalize().scale(motMp).addY(rand.nextFloat(-0.005, 0.005))
			
			val instance = TerritoryInstance.fromPos(posX.floorToInt(), posZ.floorToInt())
			val center = instance?.centerPoint
			
			if (center != null){
				val posVec = Vec(posX, posY, posZ)
				val voidFactor = TerritoryVoid.getVoidFactor(world, posVec)
				
				if (voidFactor >= TerritoryVoid.INSTANT_DEATH_FACTOR){
					setExpired()
				}
				else{
					motionVec = motionVec.offsetTowards(posVec.directionTowards(center.add(rand.nextVector(64.0))), voidFactor * 0.15)
				}
			}
		}
		
		override fun tick(){
			super.tick()
			
			motionVec = motionVec.scale(0.996)
			
			particleAlpha = if (age >= maxAge - 3)
				max(0F, particleAlpha - 0.25F)
			else
				min(1F, particleAlpha + 0.4F)
		}
		
		override fun getBrightnessForRender(partialTicks: Float): Int{
			return 0
		}
		
		override fun getRenderType(): IParticleRenderType{
			return PARTICLE_SHEET_TRANSLUCENT
		}
	}
}
