package chylex.hee.game.block.components

import net.minecraft.tileentity.TileEntity

fun interface IBlockEntityComponent {
	fun create(): TileEntity
}
