package chylex.hee.client.render

import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.world.ClientWorld
import net.minecraftforge.client.ISkyRenderHandler

object EmptyRenderer : ISkyRenderHandler {
	@Sided(Side.CLIENT)
	override fun render(ticks: Int, partialTicks: Float, matrix: MatrixStack, world: ClientWorld, mc: Minecraft) {}
}
