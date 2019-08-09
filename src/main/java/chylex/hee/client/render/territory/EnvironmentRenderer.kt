package chylex.hee.client.render.territory
import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.system.util.remapRange
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.EntityRenderer
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.pow

class EnvironmentRenderer(private vararg val renderers: IRenderHandler) : IRenderHandler(){
	companion object{
		val currentSkyAlpha
			@SideOnly(Side.CLIENT)
			get() = remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-1F)..(0.5F), (1F)..(0F)).coerceIn(0F, 1F)
		
		val currentFogDensityMp
			@SideOnly(Side.CLIENT)
			get() = 1F + (9F * remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-0.5F)..(1F), (0F)..(1F)).coerceIn(0F, 1F).pow(1.5F))
	}
	
	@SideOnly(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){
		if (mc.gameSettings.anaglyph && EntityRenderer.anaglyphField != 0){
			return
		}
		
		GL.depthMask(false)
		
		for(renderer in renderers){
			renderer.render(partialTicks, world, mc)
		}
		
		GL.depthMask(true)
	}
}
