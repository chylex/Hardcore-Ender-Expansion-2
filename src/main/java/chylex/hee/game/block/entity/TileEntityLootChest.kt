package chylex.hee.game.block.entity
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.init.ModSounds
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.playServer
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.setStack
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryBasic
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import java.util.UUID

class TileEntityLootChest : TileEntityBaseChest(){
	private companion object{
		private const val SLOT_COUNT = 9 * 3
		
		private fun createInventory() = InventoryBasic("[Loot Chest]", false, SLOT_COUNT)
		
		private fun createInventoryClone(source: IInventory) = createInventory().also {
			for((slot, stack) in source.nonEmptySlots){
				it.setStack(slot, stack.copy())
			}
		}
	}
	
	override val defaultName = "gui.hee.loot_chest.title"
	override val soundOpening = ModSounds.BLOCK_LOOT_CHEST_OPEN
	
	private val sourceInventory = createInventory() // TODO add support for loot tables
	private val playerInventories = mutableMapOf<UUID, IInventory>()
	
	override fun playChestSound(sound: SoundEvent){
		sound.playServer(world, pos, SoundCategory.BLOCKS, volume = 0.5F)
	}
	
	override fun getInventoryFor(player: EntityPlayer): IInventory{
		return if (world.isRemote)
			getInventoryForClient(player)
		else
			getInventoryForServer(player)
	}
	
	// Sided inventory handling
	
	private fun getInventoryForClient(player: EntityPlayer): IInventory{
		return if (player.isCreative)
			InventoryBasic("$defaultName.creative", false, SLOT_COUNT)
		else
			InventoryBasic(name, hasCustomName(), SLOT_COUNT)
	}
	
	private fun getInventoryForServer(player: EntityPlayer): IInventory{
		return if (player.isCreative)
			sourceInventory
		else
			playerInventories.computeIfAbsent(player.uniqueID){ createInventoryClone(sourceInventory) }
	}
	
	// Serialization
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		super.writeNBT(nbt, context)
		
		if (context == STORAGE){
			saveInventory("SourceInventory", sourceInventory)
			
			setTag("PlayerInventories", NBTTagCompound().also {
				for((uuid, inventory) in playerInventories){
					it.saveInventory(uuid.toString(), inventory)
				}
			})
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		super.readNBT(nbt, context)
		
		if (context == STORAGE){
			loadInventory("SourceInventory", sourceInventory)
			
			with(getCompoundTag("PlayerInventories")){
				for(key in keySet){
					val uuid = try{
						UUID.fromString(key)
					}catch(e: Exception){
						HEE.log.error("[TileEntityLootChest] could not parse UUID: $key")
						continue
					}
					
					playerInventories[uuid] = createInventory().also { loadInventory(key, it) }
				}
			}
		}
	}
}
