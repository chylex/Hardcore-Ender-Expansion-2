package chylex.hee.game.item.components

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.world.World

fun interface IUseItemOnEntityComponent {
	fun use(world: World, target: LivingEntity, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResultType
}
