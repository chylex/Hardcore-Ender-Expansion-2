package chylex.hee.game.entity.living.controller
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.controller.LookController

class EntityLookWhileJumping(mob: MobEntity) : LookController(mob){
	override fun tick(){
		if (mob.onGround){
			isLooking = false
		}
		else{
			super.tick()
		}
	}
}
