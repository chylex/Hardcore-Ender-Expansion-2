package chylex.hee.game.particle.base
import chylex.hee.system.util.color.IntColor
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
abstract class ParticleBase(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : Particle(world, posX, posY, posZ, motX, motY, motZ){
	protected var motionVec: Vec3d
		get() = Vec3d(motionX, motionY, motionZ)
		set(value){
			motionX = value.x
			motionY = value.y
			motionZ = value.z
		}
	
	protected fun loadColor(color: Int){
		loadColor(IntColor(color))
	}
	
	protected fun loadColor(color: IntColor){
		particleRed = color.red / 255F
		particleGreen = color.green / 255F
		particleBlue = color.blue / 255F
	}
	
	protected fun interpolateAge(baseValue: Float, fadeInDuration: Int = 0, fadeOutDuration: Int = 0): Float{
		val fadeOutAfter = particleMaxAge - fadeOutDuration
		
		return baseValue * when{
			particleAge < fadeInDuration -> particleAge.toFloat() / fadeInDuration
			particleAge > fadeOutAfter   -> 1F - ((particleAge - fadeOutAfter).toFloat() / fadeOutDuration)
			else                         -> 1F
		}
	}
}
