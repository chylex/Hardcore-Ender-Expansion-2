package chylex.hee.game.mechanics.explosion
import chylex.hee.HEE
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.entity.item.TNTEntity
import net.minecraft.entity.item.minecart.TNTMinecartEntity
import net.minecraft.world.Explosion.Mode
import net.minecraftforge.event.world.ExplosionEvent

@SubscribeAllEvents(modid = HEE.ID)
object VanillaExplosions{
	@SubscribeEvent
	fun onExplosionStart(e: ExplosionEvent.Start){
		val explosion = e.explosion
		val exploder = explosion.exploder ?: return
		
		if (explosion.mode == Mode.BREAK && exploder.javaClass.let { it === TNTEntity::class.java || it === TNTMinecartEntity::class.java }){
			explosion.mode = Mode.DESTROY
		}
	}
}
