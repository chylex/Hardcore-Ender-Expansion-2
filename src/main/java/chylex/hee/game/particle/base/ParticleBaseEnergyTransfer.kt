package chylex.hee.game.particle.base
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

@Sided(Side.CLIENT)
abstract class ParticleBaseEnergyTransfer(world: World, posX: Double, posY: Double, posZ: Double) : ParticleBaseEnergy(world, posX, posY, posZ, 0.0, 0.0, 0.0){
	protected abstract val targetPos: Vec3d
	
	init{
		particleMaxAge = 200
	}
	
	protected fun setupMotion(speed: Double){
		motionVec = targetPos.subtract(posX, posY, posZ).normalize().scale(speed)
	}
	
	override fun onUpdate(){
		val prevMot = motionVec
		
		if (targetPos.squareDistanceTo(posX, posY, posZ) <= prevMot.lengthSquared()){
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
			motionVec = prevMot
		}
	}
}
