package chylex.hee.game.block.entity
import chylex.hee.HEE
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.init.ModSounds
import chylex.hee.init.ModTileEntities
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.playServer
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.setStack
import chylex.hee.system.util.use
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import java.util.UUID

class TileEntityLootChest(type: TileEntityType<TileEntityLootChest>) : TileEntityBaseChest(type){
	constructor() : this(ModTileEntities.LOOT_CHEST)
	
	private companion object{
		private const val SLOT_COUNT = 9 * 3
		
		private const val SOURCE_INV_TAG = "SourceInventory"
		private const val PLAYER_INV_TAG = "PlayerInventories"
		
		private fun createInventory() = Inventory(SLOT_COUNT)
		
		private fun createInventoryClone(source: IInventory) = createInventory().also {
			for((slot, stack) in source.nonEmptySlots){
				it.setStack(slot, stack.copy())
			}
		}
	}
	
	override val defaultName = TextComponentTranslation("gui.hee.loot_chest.title")
	override val soundOpening = ModSounds.BLOCK_LOOT_CHEST_OPEN
	
	val sourceInventory = createInventory() // TODO add support for loot tables
	
	private val playerInventories = mutableMapOf<UUID, IInventory>()
	
	override fun playChestSound(sound: SoundEvent){
		sound.playServer(wrld, pos, SoundCategory.BLOCKS, volume = 0.5F)
	}
	
	override fun getInventoryFor(player: EntityPlayer): IInventory{
		return if (wrld.isRemote)
			getInventoryForClient(player)
		else
			getInventoryForServer(player)
	}
	
	// Sided inventory handling
	
	private fun getInventoryForClient(player: EntityPlayer): IInventory{
		return createInventory()
		/* UPDATE
		return if (player.isCreative)
			InventoryBasic("$defaultName.creative", false, SLOT_COUNT)
		else
			InventoryBasic(name, hasCustomName(), SLOT_COUNT)*/
	}
	
	private fun getInventoryForServer(player: EntityPlayer): IInventory{
		return if (player.isCreative)
			sourceInventory
		else
			playerInventories.getOrPut(player.uniqueID){ createInventoryClone(sourceInventory) }
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == STORAGE){
			saveInventory(SOURCE_INV_TAG, sourceInventory)
			
			put(PLAYER_INV_TAG, TagCompound().also {
				for((uuid, inventory) in playerInventories){
					it.saveInventory(uuid.toString(), inventory)
				}
			})
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == STORAGE){
			loadInventory(SOURCE_INV_TAG, sourceInventory)
			
			with(getCompound(PLAYER_INV_TAG)){
				for(key in keySet()){
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
