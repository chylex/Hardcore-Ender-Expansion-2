package chylex.hee.game.item.components

import chylex.hee.game.world.util.BlockEditor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class PlayerUseItemOnBlockComponent : IUseItemOnBlockComponent {
	protected open val requiresEditPermission
		get() = true
	
	final override fun use(world: World, pos: BlockPos, context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		val heldItem = player.getHeldItem(context.hand)
		
		if (requiresEditPermission && !BlockEditor.canEdit(pos, player, heldItem)) {
			return FAIL
		}
		
		return use(world, pos, player, heldItem, context)
	}
	
	protected abstract fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType
}
