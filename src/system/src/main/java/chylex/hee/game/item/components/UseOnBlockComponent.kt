package chylex.hee.game.item.components

import chylex.hee.system.migration.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface UseOnBlockComponent { // TODO use as objects instead of inheriting the interface on Item
	fun useOnBlock(world: World, pos: BlockPos, player: EntityPlayer, item: ItemStack, ctx: ItemUseContext): ActionResultType?
}
