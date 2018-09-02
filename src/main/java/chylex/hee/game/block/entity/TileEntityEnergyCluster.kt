package chylex.hee.game.block.entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable

class TileEntityEnergyCluster : TileEntityBase(), ITickable{
	override fun update(){
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
	}
	
	override fun hasFastRenderer(): Boolean = true
	override fun canRenderBreaking(): Boolean = false
}
