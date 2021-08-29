package chylex.hee.game.territory.description

import chylex.hee.game.territory.system.ITerritoryDescription
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.system.random.nextFloat
import chylex.hee.util.color.RGB
import java.util.Random

object Territory_EnderCity : ITerritoryDescription {
	override val difficulty
		get() = TerritoryDifficulty.HOSTILE
	
	override val colors = object : TerritoryColors() {
		override val tokenTop    = RGB(172, 123, 172)
		override val tokenBottom = RGB(214, 216, 157)
		
		override val portalSeed = 3527911999188999811L
		
		override fun nextPortalColor(rand: Random, color: FloatArray) {
			color[0] = rand.nextFloat(0.5F, 1F)
			color[1] = color[0] - rand.nextFloat(0F, 0.2F)
			color[2] = rand.nextFloat(0.1F, 0.3F)
		}
	}
	
	override val environment = object : TerritoryEnvironment() {
		override val fogColor = RGB(240, 255, 32).asVec
		override val fogDensity = 0.001F
		
		override val voidRadiusMpXZ = 1.1F
		override val voidRadiusMpY = 1.1F
	}
}
