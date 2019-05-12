package chylex.hee.client.render.territory
import chylex.hee.client.render.util.GL
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.EntityRenderer
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EnvironmentRenderer(private vararg val renderers: IRenderHandler) : IRenderHandler(){
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
