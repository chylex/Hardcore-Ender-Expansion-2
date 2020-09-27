package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.init.ModTileEntities
import chylex.hee.system.serialization.TagCompound
import net.minecraft.tileentity.TileEntityType

class TileEntityInfusedTNT(type: TileEntityType<TileEntityInfusedTNT>) : TileEntityBase(type){
	constructor() : this(ModTileEntities.INFUSED_TNT)
	
	var infusions = InfusionList.EMPTY
	
	override fun writeNBT(nbt: TagCompound, context: Context){
		if (context == STORAGE){
			InfusionTag.setList(nbt, infusions)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context){
		if (context == STORAGE){
			infusions = InfusionTag.getList(nbt)
		}
	}
}
