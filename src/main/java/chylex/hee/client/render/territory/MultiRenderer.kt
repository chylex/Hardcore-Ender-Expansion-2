package chylex.hee.client.render.territory
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.world.ClientWorld

class MultiRenderer(private vararg val renderers: AbstractEnvironmentRenderer) : AbstractEnvironmentRenderer(){
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, partialTicks: Float){
		for(renderer in renderers){
			renderer.render(world, partialTicks)
		}
	}
}
