package chylex.hee.game.mechanics.table.interfaces

import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.util.nbt.TagCompound

interface ITableProcessSerializer {
	fun writeToNBT(process: ITableProcess): TagCompound
	fun readFromNBT(table: TileEntityBaseTable, nbt: TagCompound): ITableProcess
}
