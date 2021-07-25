package chylex.hee.game.entity.technical

import chylex.hee.game.entity.IHeeEntityType
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.IPacket
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

@Suppress("LeakingThis")
abstract class EntityTechnicalBase(type: EntityType<out EntityTechnicalBase>, world: World) : Entity(type, world) {
	open class BaseType<T : EntityTechnicalBase> : IHeeEntityType<T> {
		final override val size
			get() = EntitySize(0F)
		
		override val tracker
			get() = EntityTrackerInfo.Defaults.TECHNICAL
		
		final override val isImmuneToFire
			get() = true
	}
	
	init {
		noClip = true
		isInvulnerable = true
		setNoGravity(true)
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun tick() {
		world.profiler.startSection("entityBaseTick")
		
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		prevRotationPitch = rotationPitch
		prevRotationYaw = rotationYaw
		
		firstUpdate = false
		
		world.profiler.endSection()
	}
	
	override fun doBlockCollisions() {}
	override fun doesEntityNotTriggerPressurePlate() = true
	override fun canBeAttackedWithItem() = false
	override fun canBeCollidedWith() = false
	override fun canBePushed() = false
	override fun canTriggerWalking() = false
}
