package chylex.hee.game.particle.base
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
abstract class ParticleBaseEnergyTransfer(world: World, posX: Double, posY: Double, posZ: Double) : ParticleBaseEnergy(world, posX, posY, posZ, 0.0, 0.0, 0.0){
	protected abstract val targetPos: Vec3d
	
	init{
		particleMaxAge = 200
	}
	
	protected fun loadColor(color: Int){
		particleRed = ((color shr 16) and 255) / 255F
		particleGreen = ((color shr 8) and 255) / 255F
		particleBlue = (color and 255) / 255F
	}
	
	protected fun setupMotion(speed: Double){
		val motion = targetPos.subtract(posX, posY, posZ).normalize().scale(speed)
		motionX = motion.x
		motionY = motion.y
		motionZ = motion.z
	}
	
	override fun onUpdate(){
		val prevMotX = motionX
		val prevMotY = motionY
		val prevMotZ = motionZ
		
		if (targetPos.squareDistanceTo(posX, posY, posZ) <= Vec3d(motionX, motionY, motionZ).lengthSquared()){
			setExpired()
		}
		
		super.onUpdate()
		
		if (isExpired){
			return
		}
		
		if (targetPos.squareDistanceTo(posX, posY, posZ) >= targetPos.squareDistanceTo(prevPosX, prevPosY, prevPosZ)){
			posX = prevPosX
			posY = prevPosY
			posZ = prevPosZ
			setExpired()
		}
		else{
			motionX = prevMotX
			motionY = prevMotY
			motionZ = prevMotZ
		}
	}
}
