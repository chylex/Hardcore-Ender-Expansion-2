package chylex.hee.game.world.territory.descriptions
import chylex.hee.client.render.territory.lightmaps.ILightmap
import chylex.hee.client.render.territory.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.util.MC
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import net.minecraft.init.MobEffects
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumSkyBlock.BLOCK
import net.minecraft.world.EnumSkyBlock.SKY
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

object Territory_ForgottenTombs : ITerritoryDescription{
	override val colors = object : TerritoryColors(){
		override val tokenTop    = RGB(211, 212, 152)
		override val tokenBottom = RGB(160, 151, 116)
		
		override val portalSeed = 410L
		
		override fun nextPortalColor(rand: Random, color: FloatArray){
			if (rand.nextBoolean()){
				color[0] = rand.nextFloat(0.65F, 0.9F)
				color[1] = rand.nextFloat(0.45F, 0.7F)
				color[2] = rand.nextFloat(0.15F, 0.4F)
			}
			else{
				color.fill(rand.nextFloat(0.95F, 1F))
			}
		}
	}
	
	private const val MAX_FOG_DENSITY = 0.069F
	
	override val environment = object : TerritoryEnvironment(){
		override val fogColor
			get() = (fogDensity / 0.275F).let { Vec3d(0.15 + it, 0.08 + it, 0.03) }
		
		override val fogDensity
			get() = currentFogDensity.get(MC.partialTicks)
		
		override val skyLight = 15
		
		override val voidRadiusMpXZ = 1.35F
		override val voidRadiusMpY = 0.975F
		override val voidCenterOffset = Vec3d(0.0, -8.0, 0.0)
		
		override val lightmap = object : ILightmap{
			override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
				val blockFactor = calcLightFactor(blockLight)
				
				colors[0] = (blockLight * 0.9F) + skyLight + 0.12F
				colors[1] = (blockFactor * 0.7F) + (skyLight * 0.8F) + 0.08F
				colors[2] = (blockFactor * 0.5F) + (skyLight * 1.2F) + (0.09F * nightVisionFactor)
			}
		}
		
		private val currentFogDensity = LerpedFloat(MAX_FOG_DENSITY)
		private var nightVisionFactor = 0F
		
		@SideOnly(Side.CLIENT)
		override fun setupClient(){
			tickClient()
			currentFogDensity.updateImmediately(MAX_FOG_DENSITY * 0.8F)
		}
		
		@SideOnly(Side.CLIENT)
		override fun tickClient(){
			val player = MC.player
			val pos = player?.lookPosVec?.let(::Pos)
			
			val light: Float
			
			if (pos == null){
				light = 1F
			}
			else{
				val world = player.world
				
				var levelBlock = 0
				var levelSky = 0
				
				for(offset in pos.allInCenteredBoxMutable(1, 1, 1)){
					levelBlock = max(levelBlock, world.getLightFor(BLOCK, offset))
					levelSky = max(levelSky, world.getLightFor(SKY, offset))
				}
				
				light = max(levelBlock / 15F, levelSky / 12F)
			}
			
			val prev = currentFogDensity.currentValue
			val next = MAX_FOG_DENSITY - (light.pow(0.2F) * 0.85F * MAX_FOG_DENSITY)
			val speed = if (next > prev) 0.025F else 0.055F
			
			currentFogDensity.update(prev + (next - prev) * speed)
			nightVisionFactor = if (player?.isPotionActive(MobEffects.NIGHT_VISION) == true) 1F else 0F
		}
	}
}
