package chylex.hee.game.particle.base
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

@Sided(Side.CLIENT)
abstract class ParticleBase(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : Particle(world, posX, posY, posZ, motX, motY, motZ){
	protected var motionVec: Vec3d
		get() = Vec3d(motionX, motionY, motionZ)
		set(value){
			motionX = value.x
			motionY = value.y
			motionZ = value.z
		}
	
	protected var age
		get() = particleAge
		set(value){ particleAge = value }
	
	protected var maxAge
		get() = particleMaxAge
		@JvmName("setMaxAge2") set(value){ particleMaxAge = value }
	
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
}
