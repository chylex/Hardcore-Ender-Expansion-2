package chylex.hee.client.render.util
import net.minecraft.block.Block
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.client.renderer.color.IItemColor

const val NO_TINT = -1

fun IBlockColor.asItem(block: Block) = IItemColor { _, tintIndex -> colorMultiplier(block.defaultState, null, null, tintIndex) }
