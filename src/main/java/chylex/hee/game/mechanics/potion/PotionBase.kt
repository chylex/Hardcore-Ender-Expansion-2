package chylex.hee.game.mechanics.potion
import chylex.hee.game.render.util.RGB
import chylex.hee.system.Resource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class PotionBase(color: RGB, isNegative: Boolean) : Potion(isNegative, color.toInt()){
	private companion object{
		@JvmStatic private val TEX_ICONS = Resource.Custom("textures/gui/status.png")
		
		private const val ICON_SIZE = 18
		private const val TEXTURE_SIZE = 64F
	}
	
	abstract val iconX: Int
	abstract val iconY: Int
	
	@SideOnly(Side.CLIENT)
	override fun renderInventoryEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft){
		mc.textureManager.bindTexture(TEX_ICONS)
		Gui.drawScaledCustomSizeModalRect(x + 6, y + 7, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
	
	@SideOnly(Side.CLIENT)
	override fun renderHUDEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft, alpha: Float){
		mc.textureManager.bindTexture(TEX_ICONS)
		Gui.drawScaledCustomSizeModalRect(x + 3, y + 3, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
}