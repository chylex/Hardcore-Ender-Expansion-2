package chylex.hee.game.entity

import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.item.ItemScorchingSword
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand.MAIN_HAND

@Suppress("unused")
object VanillaDamageHooks {
	@JvmStatic
	fun getDamageMultiplier(attacker: LivingEntity, target: Entity): Float {
		if (target is LivingEntity) {
			val heldItem = attacker.getHeldItem(MAIN_HAND).item
			
			if (heldItem is ItemScorchingSword) {
				return heldItem.handleDamageMultiplier(target)
			}
		}
		
		return 1F
	}
	
	@JvmStatic
	fun shouldDisableSweep(attacker: PlayerEntity, target: Entity): Boolean {
		if (target is EntityMobSpiderling) {
			return true
		}
		
		if (attacker.getHeldItem(MAIN_HAND).item is ItemScorchingSword) {
			return true
		}
		
		return false
	}
}
