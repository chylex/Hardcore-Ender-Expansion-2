package chylex.hee.game.mechanics.table.interfaces
import chylex.hee.system.util.TagCompound
import net.minecraft.world.World

interface ITableProcessSerializer{
	fun writeToNBT(process: ITableProcess): TagCompound
	fun readFromNBT(world: World, nbt: TagCompound): ITableProcess
}
