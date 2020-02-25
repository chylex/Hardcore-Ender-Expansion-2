package chylex.hee.game.mechanics.potion
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.util.color.IntColor
import net.minecraft.potion.EffectType

abstract class PotionBase(color: IntColor, kind: EffectType) : Potion(kind, color.i){
	companion object{
		const val INFINITE_DURATION = 32767
		const val INFINITE_DURATION_THRESHOLD = 32147 // values >= this threshold should be considered infinite
	}
}
