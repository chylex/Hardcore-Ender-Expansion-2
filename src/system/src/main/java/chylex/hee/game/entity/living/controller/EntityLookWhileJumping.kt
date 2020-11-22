package chylex.hee.game.entity.living.controller
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.ai.controller.LookController

class EntityLookWhileJumping(mob: EntityLiving) : LookController(mob){
	override fun tick(){
		if (mob.isOnGround){
			isLooking = false
		}
		else{
			super.tick()
		}
	}
}
