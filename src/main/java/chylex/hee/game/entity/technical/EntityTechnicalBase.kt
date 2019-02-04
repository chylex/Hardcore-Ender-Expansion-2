package chylex.hee.game.entity.technical
import net.minecraft.entity.Entity
import net.minecraft.world.World

abstract class EntityTechnicalBase(world: World) : Entity(world){
	init{
		noClip = true
		isImmuneToFire = true
		
		setSize(0F, 0F)
		setEntityInvulnerable(true)
		setNoGravity(true)
	}
	
	override fun onUpdate(){
		world.profiler.startSection("entityBaseTick")
		
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		prevRotationPitch = rotationPitch
		prevRotationYaw = rotationYaw
		
		firstUpdate = false
		
		world.profiler.endSection()
	}
	
	override fun doBlockCollisions(){}
	override fun doesEntityNotTriggerPressurePlate() = true
	override fun canBeAttackedWithItem() = false
	override fun canBeCollidedWith() = false
	override fun canBePushed() = false
	override fun canTriggerWalking() = false
}
