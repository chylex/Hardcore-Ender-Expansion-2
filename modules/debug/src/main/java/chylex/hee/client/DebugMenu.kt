package chylex.hee.client

import chylex.hee.client.util.MC
import chylex.hee.debug.benchmark.TerritoryGenerationBenchmarkScreen
import chylex.hee.util.color.RGB
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.client.event.InputEvent.KeyInputEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.glfw.GLFW

object DebugMenu {
	@SubscribeEvent
	fun onKeyPressed(e: KeyInputEvent) {
		if (e.action != GLFW.GLFW_RELEASE) {
			return
		}
		
		if (e.key == GLFW.GLFW_KEY_F12) {
			MC.instance.displayGuiScreen(DebugMenuScreen(MC.currentScreen))
		}
	}
	
	private class DebugMenuScreen(private val parentScreen: Screen?) : Screen(StringTextComponent("HEE 2 Debug")) {
		override fun init() {
			addButton(Button(width / 2 - 100, 36, 200, 20, StringTextComponent("Territory Generation Benchmark")) { MC.instance.displayGuiScreen(TerritoryGenerationBenchmarkScreen(this)) })
			addButton(Button(width / 2 - 100, height - 40, 200, 20, StringTextComponent("Close")) { closeScreen() })
		}
		
		override fun render(matrix: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
			renderBackground(matrix)
			drawCenteredString(matrix, font, title, width / 2, 15, RGB(255u).i)
			super.render(matrix, mouseX, mouseY, partialTicks)
		}
		
		override fun closeScreen() {
			MC.instance.displayGuiScreen(parentScreen)
		}
	}
}
