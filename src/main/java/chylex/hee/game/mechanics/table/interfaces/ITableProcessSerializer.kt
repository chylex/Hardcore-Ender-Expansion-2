package chylex.hee.game.mechanics.table.interfaces
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.system.util.TagCompound

interface ITableProcessSerializer{
	fun writeToNBT(process: ITableProcess): TagCompound
	fun readFromNBT(table: TileEntityBaseTable, nbt: TagCompound): ITableProcess
}
