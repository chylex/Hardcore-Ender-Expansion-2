package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

class BasicProcessSerializer(val constructor: (World, NBTTagCompound) -> ITableProcess) : ITableProcessSerializer{
	override fun writeToNBT(process: ITableProcess): NBTTagCompound{
		return process.serializeNBT()
	}
	
	override fun readFromNBT(world: World, nbt: NBTTagCompound): ITableProcess{
		return constructor(world, nbt).also { it.deserializeNBT(nbt) }
	}
}
