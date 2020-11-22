package chylex.hee.game.particle.base
import chylex.hee.system.color.IntColor
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_OPAQUE
import net.minecraft.client.particle.SpriteTexturedParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.vector.Vector3d

@Sided(Side.CLIENT)
abstract class ParticleBase(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : SpriteTexturedParticle(world, posX, posY, posZ, motX, motY, motZ){
	protected var motionVec: Vector3d
		get() = Vector3d(motionX, motionY, motionZ)
		set(value){
			motionX = value.x
			motionY = value.y
			motionZ = value.z
		}
	
	val redF
		get() = particleRed
	
	val greenF
		get() = particleGreen
	
	val blueF
		get() = particleBlue
	
	protected fun loadColor(color: Int){
		loadColor(IntColor(color))
	}
	
	protected fun loadColor(color: IntColor){
		particleRed = color.red / 255F
		particleGreen = color.green / 255F
		particleBlue = color.blue / 255F
	}
	
	protected fun interpolateAge(baseValue: Float, fadeInDuration: Int = 0, fadeOutDuration: Int = 0): Float{
		val fadeOutAfter = maxAge - fadeOutDuration
		
		return baseValue * when{
			age < fadeInDuration -> age.toFloat() / fadeInDuration
			age > fadeOutAfter   -> 1F - ((age - fadeOutAfter).toFloat() / fadeOutDuration)
			else                 -> 1F
		}
	}
	
	override fun getScale(partialTicks: Float): Float{
		return super.getScale(partialTicks) * 0.1F // mimics particle rendering from 1.12
	}
	
	override fun getRenderType(): IParticleRenderType{
		return PARTICLE_SHEET_OPAQUE
	}
}
