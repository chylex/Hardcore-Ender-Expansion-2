package chylex.hee.game.mechanics.potion
import chylex.hee.client.util.MC
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.migration.vanilla.PotionType
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.gui.DisplayEffectsScreen
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType

abstract class PotionBase(color: IntColor, kind: EffectType) : Potion(kind, color.i){
	companion object{
		private val TEX_ICONS = Resource.Custom("textures/gui/status.png")
		
		private const val ICON_SIZE = 18
		private const val TEXTURE_SIZE = 64
		
		const val INFINITE_DURATION = 32767
		const val INFINITE_DURATION_THRESHOLD = 32147 // values >= this threshold should be considered infinite
		
		@JvmStatic
		protected val PotionBase.makeType
			get() = PotionType(PotionBrewing.INFO.getValue(this).baseEffect)
	}
	
	abstract val iconX: Int
	abstract val iconY: Int
	
	@Sided(Side.CLIENT)
	override fun renderInventoryEffect(effect: EffectInstance?, gui: DisplayEffectsScreen<*>?, x: Int, y: Int, z: Float){
		MC.textureManager.bindTexture(TEX_ICONS)
		AbstractGui.blit(x + 6, y + 7, ICON_SIZE, ICON_SIZE, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
	
	@Sided(Side.CLIENT)
	override fun renderHUDEffect(effect: EffectInstance?, gui: AbstractGui?, x: Int, y: Int, z: Float, alpha: Float){
		MC.textureManager.bindTexture(TEX_ICONS)
		AbstractGui.blit(x + 3, y + 3, ICON_SIZE, ICON_SIZE, iconX.toFloat(), iconY.toFloat(), ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE)
	}
}
