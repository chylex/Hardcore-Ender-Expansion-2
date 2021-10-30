package chylex.hee.game.item.components

import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IBeforeUseItemOnBlockComponent {
	fun beforeUse(world: World, pos: BlockPos, context: ItemUseContext, stack: ItemStack): ActionResultType
}
