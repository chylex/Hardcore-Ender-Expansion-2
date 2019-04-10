package chylex.hee.client.render.territory
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object EmptyRenderer : IRenderHandler(){
	@SideOnly(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){}
}
