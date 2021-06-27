package chylex.hee.game.block.entity

import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.mechanics.dust.DustLayerInventory
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.world.FLAG_SYNC_CLIENT
import chylex.hee.init.ModTileEntities
import chylex.hee.system.forge.capability.LazyOptional
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getListOfCompounds
import chylex.hee.system.serialization.use
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY

class TileEntityJarODust(type: TileEntityType<TileEntityJarODust>) : TileEntityBase(type) {
	constructor() : this(ModTileEntities.JAR_O_DUST)
	
	companion object {
		const val DUST_CAPACITY = 256
		const val LAYERS_TAG = "Layers"
	}
	
	val layers = DustLayers(DUST_CAPACITY).apply { onUpdate { notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY) } }
	
	// Inventory
	
	private val inventoryIntake = LazyOptional(DustLayerInventory(layers, true))
	private val inventoryDispenser = LazyOptional(DustLayerInventory(layers, false))
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
		if (capability === ITEM_HANDLER_CAPABILITY) {
			if (facing == UP) {
				return inventoryIntake.cast()
			}
			else if (facing == DOWN) {
				return inventoryDispenser.cast()
			}
		}
		
		return super.getCapability(capability, facing)
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		putList(LAYERS_TAG, layers.serializeNBT())
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		layers.deserializeNBT(getListOfCompounds(LAYERS_TAG))
	}
}
