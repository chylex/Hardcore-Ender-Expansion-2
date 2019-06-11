package chylex.hee.game.world.territory.descriptions
import chylex.hee.client.util.MC
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.util.Pos
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
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
	
	private const val MAX_FOG_DENSITY = 0.087F
	
	override val environment = object : TerritoryEnvironment(){
		override val fogColor
			get() = (fogDensity / 0.275F).let { Vec3d(0.14 + it, 0.11 + it, 0.02) }
		
		override val fogDensity
			get() = currentFogDensity.get(MC.partialTicks)
		
		override val voidRadiusMpXZ = 0.975F
		override val voidRadiusMpY = 0.975F
		override val voidCenterOffset = Vec3d(0.0, -8.0, 0.0)
		
		private val currentFogDensity = LerpedFloat(MAX_FOG_DENSITY)
		
		@SideOnly(Side.CLIENT)
		override fun setupClient(){
			tickClient()
			currentFogDensity.updateImmediately(currentFogDensity.currentValue)
		}
		
		@SideOnly(Side.CLIENT)
		override fun tickClient(){
			val light = (MC.player?.let { it.world.getLight(Pos(it.lookPosVec)) } ?: 15) / 15F // TODO skylight
			
			val prev = currentFogDensity.currentValue
			val next = MAX_FOG_DENSITY - (light.pow(0.2F) * 0.85F * MAX_FOG_DENSITY)
			
			currentFogDensity.update(prev + (next - prev) * 0.05F)
		}
	}
}
