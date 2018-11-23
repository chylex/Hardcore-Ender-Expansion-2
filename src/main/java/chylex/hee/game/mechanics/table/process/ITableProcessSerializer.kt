package chylex.hee.game.mechanics.table.process
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

interface ITableProcessSerializer<T : ITableProcess>{
	fun writeToNBT(process: T): NBTTagCompound
	fun readFromNBT(world: World, nbt: NBTTagCompound): T
}
