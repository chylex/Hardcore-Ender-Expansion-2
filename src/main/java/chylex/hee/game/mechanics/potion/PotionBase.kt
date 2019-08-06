package chylex.hee.game.mechanics.potion
import chylex.hee.system.Resource
import chylex.hee.system.util.color.RGB
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class PotionBase(color: RGB, isNegative: Boolean) : Potion(isNegative, color.toInt()){
	companion object{
		private val TEX_ICONS = Resource.Custom("textures/gui/status.png")
		
		private const val ICON_SIZE = 18
		private const val TEXTURE_SIZE = 64F
		
		const val INFINITE_DURATION = 32767
		const val INFINITE_DURATION_THRESHOLD = 32147 // values >= this threshold should be considered infinite
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
