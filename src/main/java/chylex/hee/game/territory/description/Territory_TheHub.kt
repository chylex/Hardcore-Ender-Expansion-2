package chylex.hee.game.territory.description

import chylex.hee.client.render.world.SkyCubeStatic
import chylex.hee.game.Resource
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.territory.TerritoryType.FORGOTTEN_TOMBS
import chylex.hee.game.territory.system.ITerritoryDescription
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.game.territory.system.properties.TerritoryTokenHolders
import chylex.hee.system.random.nextFloat
import chylex.hee.util.color.RGB
import java.util.Random
import kotlin.math.min

object Territory_TheHub : ITerritoryDescription {
	override val difficulty
		get() = TerritoryDifficulty.PEACEFUL
	
	override val colors = object : TerritoryColors() {
		override val tokenTop    = RGB(255, 216, 131)
		override val tokenBottom = RGB(139, 138, 151)
		
		override val portalSeed = 31100L
		
		override fun nextPortalColor(rand: Random, color: FloatArray) {
			if (rand.nextInt(3) != 0) {
				color[0] = rand.nextFloat(0.75F, 0.9F)
				color[1] = rand.nextFloat(0.75F, 0.9F)
				color[2] = rand.nextFloat(0.45F, 0.6F)
			}
			else {
				color[0] = rand.nextFloat(0.35F, 0.45F)
				color[1] = rand.nextFloat(0.35F, 0.45F)
				color[2] = rand.nextFloat(0.85F, 1F)
			}
		}
	}
	
	override val environment = object : TerritoryEnvironment() {
		override val fogColor = RGB(10u).asVec
		override val fogDensity = 0.0115F
		override val fogRenderDistanceModifier = 0.01F
		
		override val voidRadiusMpXZ = 1.1F
		override val voidRadiusMpY = 2F
		
		override val renderer = SkyCubeStatic(
			texture = Resource.Custom("textures/environment/end_sky_blurry.png"),
			color = RGB(52u).asVec
		)
	}
	
	override val tokenHolders = object : TerritoryTokenHolders() {
		override fun onTick(holder: EntityTokenHolder, instance: TerritoryInstance) {
			if (holder.territoryType == FORGOTTEN_TOMBS) {
				val currentCharge = holder.currentCharge
				
				if (currentCharge < 1F) {
					holder.currentCharge = min(1F, currentCharge + (1F / 600F))
				}
			}
			else {
				super.onTick(holder, instance)
			}
		}
		
		override fun afterUse(holder: EntityTokenHolder, instance: TerritoryInstance) {
			if (holder.territoryType == FORGOTTEN_TOMBS) {
				holder.currentCharge = 0F
			}
			else {
				super.afterUse(holder, instance)
			}
		}
	}
}
