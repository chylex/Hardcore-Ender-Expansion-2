package chylex.hee.game.world.territory.descriptions
import chylex.hee.client.render.lightmaps.OverworldLightmap
import chylex.hee.client.render.territory.components.SkyDomeStatic
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.TerritoryDifficulty
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.math.Vec
import chylex.hee.system.random.nextFloat
import java.util.Random

object Territory_LostGarden : ITerritoryDescription{
	override val difficulty
		get() = TerritoryDifficulty.PEACEFUL
	
	override val colors = object : TerritoryColors(){
		override val tokenTop    = RGB(148, 169, 54)
		override val tokenBottom = RGB( 98,  93, 102)
		
		override val dryVines = RGB(133, 117, 72).i
		
		override val portalSeed = 5558196322L
		
		override fun nextPortalColor(rand: Random, color: FloatArray){
			return when(rand.nextInt(3)){
				in 0..1 -> {
					color[0] = rand.nextFloat(0.64F, 0.91F)
					color[1] = rand.nextFloat(0.63F, 0.94F)
					color[2] = rand.nextFloat(0.19F, 0.32F)
				}
				
				else -> {
					color[0] = rand.nextFloat(0.72F, 0.96F)
					color[1] = rand.nextFloat(0.62F, 0.82F)
					color[2] = rand.nextFloat(0.21F, 0.27F)
				}
			}
		}
	}
	
	override val environment = object : TerritoryEnvironment(){
		override val fogColor = RGB(200, 205, 170).asVec
		override val fogDensity = 0.011F
		override val fogRenderDistanceModifier = 0.008F
		
		override val skyLight = 12
		
		override val voidRadiusMpXZ = 1.25F
		override val voidRadiusMpY = 2F
		
		override val renderer = SkyDomeStatic(
			color = Vec(0.91, 0.93, 0.85),
			alpha1 = 1F,
			alpha2 = 0F
		)
		
		override val lightmap = OverworldLightmap
	}
}
