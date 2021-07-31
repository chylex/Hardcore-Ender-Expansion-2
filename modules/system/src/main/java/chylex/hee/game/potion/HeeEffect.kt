package chylex.hee.game.potion

import chylex.hee.util.color.IntColor
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectType

abstract class HeeEffect(type: EffectType, color: IntColor) : Effect(type, color.i), IHeeEffect
