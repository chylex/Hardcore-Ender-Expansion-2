package chylex.hee.game.item.util
import chylex.hee.system.util.add
import chylex.hee.system.util.center
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent

class Teleporter(
	private val resetFall: Boolean,
	private val damageDealt: Float = 0F
){
	fun toBlock(entity: EntityLivingBase, position: BlockPos): Boolean{
		return toLocation(entity, position.center.add(0.0, -0.49, 0.0))
	}
	
	fun toLocation(entity: EntityLivingBase, position: Vec3d): Boolean{
		val event = EnderTeleportEvent(entity, position.x, position.y, position.z, damageDealt)
		
		if (MinecraftForge.EVENT_BUS.post(event)){
			return false
		}
		
		if (entity.isRiding){
			entity.dismountRidingEntity()
		}
		
		if (entity.isPlayerSleeping && entity is EntityPlayer){
			entity.wakeUpPlayer(true, true, false)
		}
		
		entity.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ) // TODO fx
		
		if (resetFall){
			entity.fallDistance = 0F
		}
		
		return true
	}
}
