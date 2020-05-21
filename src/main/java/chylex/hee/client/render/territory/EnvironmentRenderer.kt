package chylex.hee.client.render.territory
import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.client.render.territory.components.SkyCubeStatic
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.remapRange
import net.minecraft.client.Minecraft
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import kotlin.math.pow

class EnvironmentRenderer(private vararg val renderers: IRenderHandler) : IRenderHandler{
	companion object{
		val currentSkyAlpha
			@Sided(Side.CLIENT)
			get() = remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-1F)..(0.5F), (1F)..(0F)).coerceIn(0F, 1F)
		
		val currentFogDensityMp
			@Sided(Side.CLIENT)
			get() = 1F + (9F * remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-0.5F)..(1F), (0F)..(1F)).coerceIn(0F, 1F).pow(1.5F))
		
		val currentRenderDistanceMp
			@Sided(Side.CLIENT)
			get() = MC.settings.renderDistanceChunks.let { if (it > 12) 0F else (1F - (it / 16.5F)).pow((it - 1) * 0.25F) }
		
		val VANILLA = EnvironmentRenderer(
			SkyCubeStatic(
				texture = Resource.Vanilla("textures/environment/end_sky.png"),
				color = (40.0 / 255.0).let { Vec3d(it, it, it) },
				distance = 100.0
			)
		)
	}
	
	@Sided(Side.CLIENT)
	override fun render(ticks: Int, partialTicks: Float, world: ClientWorld, mc: Minecraft){
		GL.depthMask(false)
		
		for(renderer in renderers){
			renderer.render(ticks, partialTicks, world, mc)
		}
		
		GL.depthMask(true)
	}
}
