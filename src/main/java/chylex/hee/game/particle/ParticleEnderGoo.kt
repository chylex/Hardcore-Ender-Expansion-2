package chylex.hee.game.particle
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.toRadians
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.PI

object ParticleEnderGoo : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	private val COLOR = FluidEnderGoo.rgbColor.let { floatArrayOf(it.red / 255F, it.green / 255F, it.blue / 255F) }
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		private var angleOffset = (rand.nextInt(5, 10) * (if (rand.nextBoolean()) 1F else -1F)).toRadians()
		
		init{
			particleTextureIndexX = rand.nextInt(8)
			particleTextureIndexY = 0
			
			val colorMp = if (rand.nextInt(10) == 0)
				rand.nextFloat(0.3F, 0.6F)
			else
				0.9F
			
			particleRed   = COLOR[0] * colorMp * rand.nextFloat(0.8F, 1F)
			particleGreen = COLOR[1] * colorMp * rand.nextFloat(0.6F, 1F)
			particleBlue  = COLOR[2] * colorMp * rand.nextFloat(0.9F, 1F)
			
			particleAngle = rand.nextFloat(0.0, PI).toFloat()
			particleScale = rand.nextFloat(1.0F, 1.4F)
			
			particleGravity = 0.15F
			
			particleMaxAge = rand.nextInt(34, 42)
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			prevParticleAngle = particleAngle
			particleAngle += angleOffset
			angleOffset *= 0.94F
			
			if (particleAge > particleMaxAge - 15){
				particleScale *= 0.9F
			}
			
			motionX += rand.nextFloat(-0.02F, 0.02F)
			motionZ += rand.nextFloat(-0.02F, 0.02F)
		}
	}
}
