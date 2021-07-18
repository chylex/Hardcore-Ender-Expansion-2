package chylex.hee.game.territory.description

import chylex.hee.game.territory.system.ITerritoryDescription
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.system.random.nextFloat
import chylex.hee.util.color.RGB
import java.util.Random

object Territory_ArcaneConjunctions : ITerritoryDescription {
	override val difficulty
		get() = TerritoryDifficulty.NEUTRAL
	
	override val colors = object : TerritoryColors() {
		override val tokenTop    = RGB(174, 104, 128)
		override val tokenBottom = RGB( 98,  93, 102)
		
		override val portalSeed = 102030405060708090L
		
		override fun nextPortalColor(rand: Random, color: FloatArray) {
			color[0] = 1F - rand.nextFloat(0F, 0.4F)
			color[1] = rand.nextFloat(0.5F, 0.9F) - rand.nextFloat(0F, 0.2F)
			color[2] = 1F - rand.nextFloat(0F, 0.5F)
		}
	}
	
	override val environment = object : TerritoryEnvironment() {
		override val fogColor = RGB(60, 50, 55).asVec
		override val fogDensity = 0.015F
		override val fogRenderDistanceModifier = 0.01F
		
		override val voidRadiusMpXZ = 1.25F
		override val voidRadiusMpY = 1.5F
		
		override val renderer = VANILLA
	}
}
