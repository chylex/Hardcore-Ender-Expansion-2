package chylex.hee.game.mechanics.energy

import chylex.hee.game.item.interfaces.IItemInterface
import chylex.hee.game.item.interfaces.getHeeInterface
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.BlockPos

interface IClusterOracleItem : IItemInterface {
	fun isPositionIgnored(stack: ItemStack, pos: BlockPos): Boolean
	
	companion object {
		fun isPositionIgnored(player: PlayerEntity, pos: BlockPos): Boolean {
			return isPositionIgnored(player, MAIN_HAND, pos) || isPositionIgnored(player, OFF_HAND, pos)
		}
		
		private fun isPositionIgnored(player: PlayerEntity, hand: Hand, pos: BlockPos): Boolean {
			val heldItem = player.getHeldItem(hand)
			val oracle = heldItem.item.getHeeInterface<IClusterOracleItem>()
			return oracle != null && oracle.isPositionIgnored(heldItem, pos)
		}
	}
}
