package chylex.hee.game.particle.base
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
abstract class ParticleBase(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : Particle(world, posX, posY, posZ, motX, motY, motZ){
	protected val mc: Minecraft
		get() = Minecraft.getMinecraft()
	
	protected val settings: GameSettings
		get() = Minecraft.getMinecraft().gameSettings
	
	protected var motionVec: Vec3d
		get() = Vec3d(motionX, motionY, motionZ)
		set(value){
			motionX = value.x
			motionY = value.y
			motionZ = value.z
		}
	
	protected fun loadColor(color: Int){
		particleRed = ((color shr 16) and 255) / 255F
		particleGreen = ((color shr 8) and 255) / 255F
		particleBlue = (color and 255) / 255F
	}
}
