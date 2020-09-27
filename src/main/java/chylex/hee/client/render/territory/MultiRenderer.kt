package chylex.hee.client.render.territory
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.world.ClientWorld

class MultiRenderer(private vararg val renderers: AbstractEnvironmentRenderer) : AbstractEnvironmentRenderer(){
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float){
		for(renderer in renderers){
			renderer.render(world, matrix, partialTicks)
		}
	}
}
