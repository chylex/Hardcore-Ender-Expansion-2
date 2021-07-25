package chylex.hee.game.block.entity

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity

interface IHeeTileEntityType<T : TileEntity> {
	val blocks: Array<out Block>
}
