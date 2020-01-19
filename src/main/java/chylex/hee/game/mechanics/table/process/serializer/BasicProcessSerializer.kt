package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.system.util.TagCompound

class BasicProcessSerializer(val constructor: (TileEntityBaseTable, TagCompound) -> ITableProcess) : ITableProcessSerializer{
	override fun writeToNBT(process: ITableProcess): TagCompound{
		return process.serializeNBT()
	}
	
	override fun readFromNBT(table: TileEntityBaseTable, nbt: TagCompound): ITableProcess{
		return constructor(table, nbt).also { it.deserializeNBT(nbt) }
	}
}
