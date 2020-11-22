package chylex.hee.game.world.territory.descriptions
import chylex.hee.client.MC
import chylex.hee.client.render.lightmaps.ILightmap
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.territory.components.SkyPlaneTopFoggy
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.TerritoryDifficulty
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.LerpedFloat
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.nextFloat
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

object Territory_ForgottenTombs : ITerritoryDescription{
	override val difficulty
		get() = TerritoryDifficulty.HOSTILE
	
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
			get() = (fogDensity / 0.275F).let { Vector3d(0.15 + it, 0.08 + it, 0.03) }
		
		override val fogDensity
			get() = currentFogDensity.get(MC.partialTicks)
		
		override val fogRenderDistanceModifier = 0.003F
		
		override val skyLight = 15
		
		override val voidRadiusMpXZ = 1.35F
		override val voidRadiusMpY = 0.975F
		override val voidCenterOffset = Vector3d(0.0, -8.0, 0.0)
		
		override val renderer = SkyPlaneTopFoggy(
			texture = Resource.Custom("textures/environment/stars.png"),
			color = Vector3d(0.58, 0.58, 0.54),
			rescale = 29F,
			distance = 65F,
			width = 300F
		)
		
		override val lightmap = object : ILightmap{
			override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
				val blockFactor = calcLightFactor(blockLight)
				
				colors.x = (blockLight * 0.9F) + skyLight + 0.12F
				colors.y = (blockFactor * 0.7F) + (skyLight * 0.8F) + 0.08F
				colors.z = (blockFactor * 0.5F) + (skyLight * 1.2F) + (0.09F * nightVisionFactor)
			}
		}
		
		private val currentFogDensity = LerpedFloat(MAX_FOG_DENSITY)
		private var nightVisionFactor = 0F
		
		@Sided(Side.CLIENT)
		override fun setupClient(player: EntityPlayer){
			tickClient(player)
			currentFogDensity.updateImmediately(MAX_FOG_DENSITY * 0.8F)
		}
		
		@Sided(Side.CLIENT)
		override fun tickClient(player: EntityPlayer){
			val world = player.world
			val pos = Pos(player.lookPosVec)
			
			var levelBlock = 0
			var levelSky = 0
			
			for(offset in pos.allInCenteredBoxMutable(1, 1, 1)){
				levelBlock = max(levelBlock, world.getLightFor(BLOCK, offset))
				levelSky = max(levelSky, world.getLightFor(SKY, offset))
			}
			
			val light = max(levelBlock / 15F, levelSky / 12F)
			
			val prev = currentFogDensity.currentValue
			val next = MAX_FOG_DENSITY - (light.pow(0.2F) * 0.85F * MAX_FOG_DENSITY)
			val speed = if (next > prev) 0.025F else 0.055F
			
			currentFogDensity.update(offsetTowards(prev, next, speed))
			nightVisionFactor = if (player.isPotionActive(Potions.NIGHT_VISION)) 1F else 0F
		}
	}
}
