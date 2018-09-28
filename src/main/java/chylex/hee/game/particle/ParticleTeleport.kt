package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleTeleport : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		minLifespan: Int = 35,
		maxLifespan: Int = 50,
		minScale: Float = 1.25F,
		maxScale: Float = 1.45F
	) : IParticleData.Static(intArrayOf(
		minLifespan,
		maxLifespan,
		(minScale * 100F).floorToInt(),
		(maxScale * 100F).floorToInt()
	))
	
	private val DEFAULT_DATA = Data()
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : Particle(world, posX, posY, posZ, motX, motY, motZ){
		private val initialScale: Float
		
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			initialScale = rand.nextFloat(data[2] * 0.01F, data[3] * 0.01F)
			
			motionX = motX
			motionY = motY
			motionZ = motZ
			
			particleGravity = 0F
			
			particleTextureIndexX = rand.nextInt(8)
			particleTextureIndexY = 0
			
			particleBlue = rand.nextFloat(0.4F, 1.0F)
			particleGreen = particleBlue * 0.3F
			particleRed = particleBlue * 0.9F
			
			particleMaxAge = rand.nextInt(data[0], data[1])
		}
		
		override fun move(x: Double, y: Double, z: Double){ // skips collision checking
			boundingBox = boundingBox.offset(x, y, z)
			resetPositionToBB()
		}
		
		override fun renderParticle(buffer: BufferBuilder, entity: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float){
			particleScale = initialScale * (1F - (particleAge + partialTicks) / (particleMaxAge + 1F))
			super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
		}
	}
}
