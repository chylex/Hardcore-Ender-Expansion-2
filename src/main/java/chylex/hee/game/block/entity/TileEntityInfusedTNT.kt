package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import net.minecraft.nbt.NBTTagCompound

class TileEntityInfusedTNT : TileEntityBase(){
	var infusions = InfusionList.EMPTY
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context){
		if (context == STORAGE){
			InfusionTag.setList(nbt, infusions)
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context){
		if (context == STORAGE){
			infusions = InfusionTag.getList(nbt)
		}
	}
}
