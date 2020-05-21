package chylex.hee.game.world.territory.descriptions
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.nextFloat
import net.minecraft.util.math.Vec3d
import java.util.Random

object Territory_ObsidianTowers : ITerritoryDescription{
	override val colors = object : TerritoryColors(){
		override val tokenTop    = RGB(146, 130, 185)
		override val tokenBottom = RGB( 81, 173, 250)
		
		override val portalSeed = 5555L
		
		override fun nextPortalColor(rand: Random, color: FloatArray){
			if (rand.nextBoolean()){
				color[0] = rand.nextFloat(0.2F, 0.5F)
				color[1] = rand.nextFloat(0.7F, 0.9F)
				color[2] = 1F
			}
			else{
				color[0] = rand.nextFloat(0.5F, 0.7F)
				color[1] = 0.5F
				color[2] = rand.nextFloat(0.9F, 1F)
			}
		}
	}
	
	override val environment = object : TerritoryEnvironment(){
		override val fogColor = Vec3d(0.0, 0.0, 0.0)
		override val fogDensity = 0.01F
		override val fogRenderDistanceModifier = 0.005F
		
		override val voidRadiusMpXZ = 2F
		override val voidRadiusMpY = 2.5F
	}
}
