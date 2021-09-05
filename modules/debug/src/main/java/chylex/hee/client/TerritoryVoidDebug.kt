package chylex.hee.client

import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.client.util.MC
import chylex.hee.game.world.isInEndDimension
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object TerritoryVoidDebug {
	@SubscribeEvent
	fun onRenderGameOverlayText(e: RenderGameOverlayEvent.Text) {
		if (MC.settings.showDebugInfo && MC.player?.isInEndDimension == true) {
			with(e.left) {
				add("")
				add("End Void Factor: ${"%.3f".format(TerritoryRenderer.currentVoidFactor)}")
			}
		}
	}
}
