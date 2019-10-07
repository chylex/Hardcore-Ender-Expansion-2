package chylex.hee.client.render.territory
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraftforge.client.IRenderHandler

object EmptyRenderer : IRenderHandler(){
	@Sided(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){}
}
