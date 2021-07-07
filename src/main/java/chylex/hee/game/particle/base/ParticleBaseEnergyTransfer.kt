package chylex.hee.game.particle.base

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.vector.Vector3d

@Sided(Side.CLIENT)
abstract class ParticleBaseEnergyTransfer(world: ClientWorld, posX: Double, posY: Double, posZ: Double) : ParticleBaseEnergy(world, posX, posY, posZ, 0.0, 0.0, 0.0) {
	protected abstract val targetPos: Vector3d
	
	init {
		maxAge = 200
	}
	
	protected fun setupMotion(speed: Double) {
		motionVec = targetPos.subtract(posX, posY, posZ).normalize().scale(speed)
	}
	
	override fun tick() {
		val prevMot = motionVec
		
		if (targetPos.squareDistanceTo(posX, posY, posZ) <= prevMot.lengthSquared()) {
			setExpired()
		}
		
		super.tick()
		
		if (isExpired) {
			return
		}
		
		if (targetPos.squareDistanceTo(posX, posY, posZ) >= targetPos.squareDistanceTo(prevPosX, prevPosY, prevPosZ)) {
			posX = prevPosX
			posY = prevPosY
			posZ = prevPosZ
			setExpired()
		}
		else {
			motionVec = prevMot
		}
	}
}
