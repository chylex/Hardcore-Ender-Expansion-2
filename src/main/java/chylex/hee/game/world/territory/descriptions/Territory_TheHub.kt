package chylex.hee.game.world.territory.descriptions
import chylex.hee.client.render.territory.EmptyRenderer
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.util.color.HCL
import chylex.hee.system.util.nextFloat
import net.minecraft.util.math.Vec3d
import java.util.Random

object Territory_TheHub : ITerritoryDescription{
	override val colors = object : TerritoryColors(){
		override val tokenTop    = HCL(270.0,  10F, 58F)
		override val tokenBottom = HCL( 40.0, 100F, 94F)
		
		override val portalSeed = 31100L
		
		override fun nextPortalColor(rand: Random, color: FloatArray){
			if (rand.nextInt(3) != 0){
				color[0] = rand.nextFloat(0.75F, 0.9F)
				color[1] = rand.nextFloat(0.75F, 0.9F)
				color[2] = rand.nextFloat(0.45F, 0.6F)
			}
			else{
				color[0] = rand.nextFloat(0.35F, 0.45F)
				color[1] = rand.nextFloat(0.35F, 0.45F)
				color[2] = rand.nextFloat(0.85F, 1F)
			}
		}
	}
	
	override val environment = object : TerritoryEnvironment(){
		override val skyColor = Vec3d(1.0, 1.0, 1.0)
		override val skyRenderer = EmptyRenderer
		
		override val fogColor = Vec3d(0.0, 0.0, 0.0)
		override val fogDensity = 0F
	}
}
