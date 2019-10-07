package chylex.hee.game.particle.util
import chylex.hee.HEE
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.client.event.TextureStitchEvent

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
object ParticleTexture{
	lateinit var PIXEL: TextureAtlasSprite private set
	lateinit var STAR: TextureAtlasSprite private set
	
	private val TEX_PIXEL = Resource.Custom("particle/pixel")
	private val TEX_STAR = Resource.Custom("particle/star")
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		with(e.map){
			registerSprite(TEX_PIXEL)
			registerSprite(TEX_STAR)
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPost(e: TextureStitchEvent.Post){
		with(e.map){
			PIXEL = getAtlasSprite(TEX_PIXEL.toString())
			STAR = getAtlasSprite(TEX_STAR.toString())
		}
	}
}
