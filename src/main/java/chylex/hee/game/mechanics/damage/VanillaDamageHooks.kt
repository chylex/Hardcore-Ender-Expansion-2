package chylex.hee.game.mechanics.damage

import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.item.ItemScorchingSword
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.entity.Entity

@Suppress("unused")
object VanillaDamageHooks {
	@JvmStatic
	fun getDamageMultiplier(attacker: EntityLivingBase, target: Entity): Float {
		if (target is EntityLivingBase) {
			val heldItem = attacker.getHeldItem(MAIN_HAND).item
			
			if (heldItem is ItemScorchingSword) {
				return heldItem.handleDamageMultiplier(target)
			}
		}
		
		return 1F
	}
	
	@JvmStatic
	fun shouldDisableSweep(attacker: EntityPlayer, target: Entity): Boolean {
		if (target is EntityMobSpiderling) {
			return true
		}
		
		if (attacker.getHeldItem(MAIN_HAND).item is ItemScorchingSword) {
			return true
		}
		
		return false
	}
}
