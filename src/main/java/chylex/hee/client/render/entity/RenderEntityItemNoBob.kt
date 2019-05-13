package chylex.hee.client.render.entity
import chylex.hee.client.util.MC
import net.minecraft.client.renderer.entity.RenderEntityItem
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderEntityItemNoBob(manager: RenderManager) : RenderEntityItem(manager, MC.itemRenderer){
	override fun shouldBob(): Boolean = false
}
