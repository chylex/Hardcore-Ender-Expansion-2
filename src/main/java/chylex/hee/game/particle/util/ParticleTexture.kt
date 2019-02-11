package chylex.hee.game.particle.util
import chylex.hee.HEE
import chylex.hee.system.Resource
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
object ParticleTexture{
	
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		with(e.map){
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPost(e: TextureStitchEvent.Post){
		with(e.map){
		}
	}
}
