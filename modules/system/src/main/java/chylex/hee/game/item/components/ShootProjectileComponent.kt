package chylex.hee.game.item.components

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

abstract class ShootProjectileComponent : IUseItemOnAirComponent {
	override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
		if (!world.isRemote) {
			world.addEntity(createEntity(world, player, hand, heldItem))
		}
		
		player.addStat(Stats.ITEM_USED[heldItem.item])
		
		if (!player.abilities.isCreativeMode) {
			heldItem.shrink(1)
		}
		
		return ActionResult.func_233538_a_(heldItem, world.isRemote)
	}
	
	protected abstract fun createEntity(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): Entity
}
