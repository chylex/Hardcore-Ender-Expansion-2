package chylex.hee.client

import chylex.hee.client.util.MC
import net.minecraftforge.client.event.InputEvent.KeyInputEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.glfw.GLFW

object GameModeToggle {
	@SubscribeEvent
	fun onKeyPressed(e: KeyInputEvent) {
		if (e.action != GLFW.GLFW_PRESS) {
			return
		}
		
		if (e.key == GLFW.GLFW_KEY_GRAVE_ACCENT) {
			val player = MC.player ?: return
			
			if (player.isCreative) {
				val ctrl = (e.modifiers and GLFW.GLFW_MOD_CONTROL) != 0
				player.sendChatMessage(if (ctrl) "/gamemode spectator" else "/gamemode survival")
			}
			else {
				player.sendChatMessage("/gamemode creative")
			}
		}
	}
}
