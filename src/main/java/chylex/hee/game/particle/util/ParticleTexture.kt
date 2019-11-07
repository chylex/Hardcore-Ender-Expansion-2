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
	lateinit var EXPERIENCE: Array<TextureAtlasSprite> private set
	lateinit var PIXEL: TextureAtlasSprite private set
	lateinit var STAR: TextureAtlasSprite private set
	
	private val TEX_EXPERIENCE = Resource.Vanilla("entity/experience_orb")
	private val TEX_PIXEL = Resource.Custom("particle/pixel")
	private val TEX_STAR = Resource.Custom("particle/star")
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		with(e.map){
			registerSprite(TEX_EXPERIENCE)
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
			
			val experienceStr = TEX_EXPERIENCE.toString()
			val experienceTex = getAtlasSprite(experienceStr)
			
			EXPERIENCE = Array(11){
				object : TextureAtlasSprite(experienceStr){
					private val minU: Float
					private val minV: Float
					private val maxU: Float
					private val maxV: Float
					
					init{
						copyFrom(experienceTex)
						
						val w = (experienceTex.maxU - experienceTex.minU) * 0.25F
						val h = (experienceTex.maxV - experienceTex.minV) * 0.25F
						
						minU = experienceTex.minU + (w * (it / 4))
						minV = experienceTex.minV + (h * (it % 4))
						maxU = minU + w
						maxV = minV + h
					}
					
					override fun getMinU() = minU
					override fun getMinV() = minV
					override fun getMaxU() = maxU
					override fun getMaxV() = maxV
				}
			}
		}
	}
}
