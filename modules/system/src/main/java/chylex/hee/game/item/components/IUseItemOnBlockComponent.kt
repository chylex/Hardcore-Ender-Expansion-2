package chylex.hee.game.item.components

import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IUseItemOnBlockComponent {
	fun use(world: World, pos: BlockPos, context: ItemUseContext): ActionResultType
}
