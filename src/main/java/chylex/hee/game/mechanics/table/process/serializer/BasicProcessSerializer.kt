package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.system.util.TagCompound
import net.minecraft.world.World

class BasicProcessSerializer(val constructor: (World, TagCompound) -> ITableProcess) : ITableProcessSerializer{
	override fun writeToNBT(process: ITableProcess): TagCompound{
		return process.serializeNBT()
	}
	
	override fun readFromNBT(world: World, nbt: TagCompound): ITableProcess{
		return constructor(world, nbt).also { it.deserializeNBT(nbt) }
	}
}
