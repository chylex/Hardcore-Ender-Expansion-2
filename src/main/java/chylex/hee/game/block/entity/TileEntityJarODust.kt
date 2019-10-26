package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.mechanics.dust.DustLayerInventory
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getListOfCompounds
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY

class TileEntityJarODust : TileEntityBase(){
	companion object{
		const val DUST_CAPACITY = 256
	}
	
	val layers = DustLayers(DUST_CAPACITY).apply { onUpdate { notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY) } }
	
	// Inventory
	
	private val inventoryIntake = DustLayerInventory(layers, true)
	private val inventoryDispenser = DustLayerInventory(layers, false)
	
	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean{
		return (capability === ITEM_HANDLER_CAPABILITY && (facing == UP || facing == DOWN)) || super.hasCapability(capability, facing)
	}
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T?{
		if (capability === ITEM_HANDLER_CAPABILITY){
			if (facing == UP){
				return ITEM_HANDLER_CAPABILITY.cast(inventoryIntake)
			}
			else if (facing == DOWN){
				return ITEM_HANDLER_CAPABILITY.cast(inventoryDispenser)
			}
		}
		
		return super.getCapability(capability, facing)
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		setList(BlockJarODust.LAYERS_TAG, layers.serializeNBT())
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		layers.deserializeNBT(getListOfCompounds(BlockJarODust.LAYERS_TAG))
	}
}
