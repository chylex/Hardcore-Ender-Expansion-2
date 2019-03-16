package chylex.hee.client.render.entity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.RenderEntityItem
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderEntityItemNoBob(manager: RenderManager) : RenderEntityItem(manager, Minecraft.getMinecraft().renderItem){
	override fun shouldBob(): Boolean = false
}
