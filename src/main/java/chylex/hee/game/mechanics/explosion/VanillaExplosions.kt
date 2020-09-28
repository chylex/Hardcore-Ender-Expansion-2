package chylex.hee.game.mechanics.explosion
import chylex.hee.HEE
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityTNTMinecart
import chylex.hee.system.migration.EntityTNTPrimed
import net.minecraft.world.Explosion.Mode
import net.minecraftforge.event.world.ExplosionEvent

@SubscribeAllEvents(modid = HEE.ID)
object VanillaExplosions{
	@SubscribeEvent
	fun onExplosionStart(e: ExplosionEvent.Start){
		val explosion = e.explosion
		val exploder = explosion.exploder ?: return
		
		if (explosion.mode == Mode.BREAK && exploder.javaClass.let { it === EntityTNTPrimed::class.java || it === EntityTNTMinecart::class.java }){
			explosion.mode = Mode.DESTROY
		}
	}
}
