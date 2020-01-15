package chylex.hee.game.block.entity
import chylex.hee.HEE
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.game.container.ContainerLootChest
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
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.text.ITextComponent
import java.util.UUID

class TileEntityLootChest(type: TileEntityType<TileEntityLootChest>) : TileEntityBaseChest(type){
	constructor() : this(ModTileEntities.LOOT_CHEST)
	
	companion object{
		const val ROWS = 3
		
		private const val SLOT_COUNT = 9 * ROWS
		
		private const val SOURCE_INV_TAG = "SourceInventory"
		private const val PLAYER_INV_TAG = "PlayerInventories"
		
		private fun createInventory() = Inventory(SLOT_COUNT)
		
		private fun createInventoryClone(source: IInventory) = createInventory().also {
			for((slot, stack) in source.nonEmptySlots){
				it.setStack(slot, stack.copy())
			}
		}
		
		fun getClientTitle(player: EntityPlayer, text: ITextComponent): ITextComponent{
			return if (player.abilities.isCreativeMode)
				TextComponentTranslation("gui.hee.loot_chest.title.creative")
			else
				text
		}
	}
	
	override val defaultName = TextComponentTranslation("gui.hee.loot_chest.title")
	override val soundOpening = ModSounds.BLOCK_LOOT_CHEST_OPEN
	
	val sourceInventory = createInventory() // TODO add support for loot tables
	
	private val playerInventories = mutableMapOf<UUID, IInventory>()
	
	override fun playChestSound(sound: SoundEvent){
		sound.playServer(wrld, pos, SoundCategory.BLOCKS, volume = 0.5F)
	}
	
	// Sided inventory handling
	
	override fun createMenu(id: Int, inventory: PlayerInventory, player: EntityPlayer): Container{
		return ContainerLootChest(id, player, createChestInventory(getInventoryForServer(player)))
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
