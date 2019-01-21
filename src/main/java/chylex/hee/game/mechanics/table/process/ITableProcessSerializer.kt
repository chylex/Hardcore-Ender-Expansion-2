package chylex.hee.game.mechanics.table.process
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

interface ITableProcessSerializer{
	fun writeToNBT(process: ITableProcess): NBTTagCompound
	fun readFromNBT(world: World, nbt: NBTTagCompound): ITableProcess
}
