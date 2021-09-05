package chylex.hee.debug.benchmark

import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.Environment
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.util.color.RGB
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import java.util.Random

class TerritoryGenerationBenchmarkScreen(private val parentScreen: Screen) : Screen(StringTextComponent("Territory Generation Benchmark")) {
	private val generated = mutableListOf<SegmentedWorld>()
	
	override fun init() {
		try {
			Environment.getDimension(World.OVERWORLD)
		} catch (e: NullPointerException) {
			println("Must be in a world!")
			closeScreen()
			return
		}
		
		val x = width / 2 - 100
		addButton(Button(x, 36, 200, 20, StringTextComponent("(All)")) { runAll() })
		
		for ((index, territory) in TerritoryType.ALL.withIndex()) {
			val y = 36 + (22 * (index + 1))
			addButton(Button(x, y, 200, 20, TranslationTextComponent(territory.translationKey)) { runOnce(territory) })
		}
		
		addButton(Button(x, height - 40, 200, 20, StringTextComponent("Close")) { closeScreen() })
	}
	
	override fun render(matrix: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
		renderBackground(matrix)
		drawCenteredString(matrix, font, title, width / 2, 15, RGB(255u).i)
		super.render(matrix, mouseX, mouseY, partialTicks)
	}
	
	override fun closeScreen() {
		MC.instance.displayGuiScreen(parentScreen)
	}
	
	private fun runAll() {
		generated.clear()
		
		for (territory in TerritoryType.ALL) {
			if (territory.gen === TerritoryType.Companion.GeneratorDummy) {
				continue
			}
			
			for (seed in 0L until 5L) {
				runImpl(territory, seed)
			}
		}
		
		HEE.log.info("[TerritoryGenerationBenchmarkScreen] done!")
	}
	
	private fun runOnce(territory: TerritoryType) {
		generated.clear()
		runImpl(territory, 0L)
	}
	
	private fun runImpl(territory: TerritoryType, seed: Long) {
		HEE.log.info("[TerritoryGenerationBenchmarkScreen] generating " + territory.name)
		
		val timeStart = System.currentTimeMillis()
		generated.add(territory.generate(Random(seed)).first)
		val timeEnd = System.currentTimeMillis()
		
		HEE.log.info("[TerritoryGenerationBenchmarkScreen] finished in ${timeEnd - timeStart} ms")
	}
}
