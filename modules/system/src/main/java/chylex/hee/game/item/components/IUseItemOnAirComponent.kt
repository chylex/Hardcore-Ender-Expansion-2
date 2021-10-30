package chylex.hee.game.item.components

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

fun interface IUseItemOnAirComponent {
	fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack>
}
