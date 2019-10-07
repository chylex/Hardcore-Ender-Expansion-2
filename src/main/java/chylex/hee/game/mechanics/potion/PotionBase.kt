package chylex.hee.game.mechanics.potion
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionType

abstract class PotionBase(color: IntColor, isNegative: Boolean) : Potion(isNegative, color.i){
	companion object{
		private val TEX_ICONS = Resource.Custom("textures/gui/status.png")
		
		private const val ICON_SIZE = 18
		private const val TEXTURE_SIZE = 64F
		
		const val INFINITE_DURATION = 32767
		const val INFINITE_DURATION_THRESHOLD = 32147 // values >= this threshold should be considered infinite
		
		@JvmStatic
		protected val PotionBase.makeType
			get() = PotionType(PotionBrewing.INFO.getValue(this).baseEffect)
	}
	
	abstract val iconX: Int
	abstract val iconY: Int
	
	@Sided(Side.CLIENT)
	override fun renderInventoryEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft){
		mc.textureManager.bindTexture(TEX_ICONS)
		Gui.drawScaledCustomSizeModalRect(x + 6, y + 7, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
	
	@Sided(Side.CLIENT)
	override fun renderHUDEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft, alpha: Float){
		mc.textureManager.bindTexture(TEX_ICONS)
		Gui.drawScaledCustomSizeModalRect(x + 3, y + 3, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
}
