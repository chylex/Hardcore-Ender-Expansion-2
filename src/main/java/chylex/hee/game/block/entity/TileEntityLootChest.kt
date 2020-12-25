package chylex.hee.game.block.entity

import chylex.hee.HEE
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.game.inventory.nonEmptySlots
import chylex.hee.game.inventory.setStack
import chylex.hee.game.world.playServer
import chylex.hee.init.ModSounds
import chylex.hee.init.ModTileEntities
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getStringOrNull
import chylex.hee.system.serialization.hasInventory
import chylex.hee.system.serialization.loadInventory
import chylex.hee.system.serialization.saveInventory
import chylex.hee.system.serialization.use
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import net.minecraft.world.storage.loot.LootParameters
import java.util.UUID

class TileEntityLootChest(type: TileEntityType<TileEntityLootChest>) : TileEntityBaseChest(type) {
	constructor() : this(ModTileEntities.LOOT_CHEST)
	
	companion object {
		const val ROWS = 3
		
		private const val SLOT_COUNT = 9 * ROWS
		
		private const val SOURCE_INV_TAG = "SourceInventory"
		private const val PLAYER_INV_TAG = "PlayerInventories"
		
		private fun createInventory() = Inventory(SLOT_COUNT)
		
		private fun createInventoryClone(source: IInventory) = createInventory().also {
			for((slot, stack) in source.nonEmptySlots) {
				it.setStack(slot, stack.copy())
			}
		}
		
		fun getClientTitle(player: EntityPlayer, text: ITextComponent): ITextComponent {
			return if (player.abilities.isCreativeMode)
				TranslationTextComponent("gui.hee.loot_chest.title.creative")
			else
				text
		}
	}
	
	override val defaultName = TranslationTextComponent("gui.hee.loot_chest.title")
	override val soundOpening = ModSounds.BLOCK_LOOT_CHEST_OPEN
	
	val sourceInventory = createInventory()
	private var sourceLootTable: String? = null
	
	private val playerInventories = mutableMapOf<UUID, IInventory>()
	
	val hasLootTable
		get() = sourceLootTable != null
	
	override fun playChestSound(sound: SoundEvent) {
		sound.playServer(wrld, pos, SoundCategory.BLOCKS, volume = 0.5F)
	}
	
	// Command handling
	
	fun resetPlayerInventories(): Int {
		val total = playerInventories.size
		playerInventories.clear()
		return total
	}
	
	fun setLootTable(resource: ResourceLocation?) {
		sourceLootTable = resource?.toString()
	}
	
	// Sided inventory handling
	
	override fun createMenu(id: Int, inventory: PlayerInventory, player: EntityPlayer): Container {
		return ContainerLootChest(id, player, createChestInventory(getInventoryForServer(player)))
	}
	
	private fun getInventoryForServer(player: EntityPlayer): IInventory {
		return if (player.isCreative)
			sourceInventory
		else
			playerInventories.getOrPut(player.uniqueID) { generateNewLoot(player) }
	}
	
	private fun generateNewLoot(player: EntityPlayer): IInventory {
		val lootTable = sourceLootTable
		
		if (lootTable == null) {
			return createInventoryClone(sourceInventory)
		}
		
		val world = wrld as ServerWorld
		val lootContext = LootContext.Builder(world)
			.withRandom(world.rand)
			.withParameter(LootParameters.POSITION, pos)
			.withParameter(LootParameters.THIS_ENTITY, player)
			.withLuck(player.luck)
			.build(LootParameterSets.CHEST)
		
		return Inventory(SLOT_COUNT).apply { Environment.getLootTable(ResourceLocation(lootTable)).fillInventory(this, lootContext) }
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == STORAGE) {
			val lootTable = sourceLootTable
			
			if (lootTable == null) {
				saveInventory(SOURCE_INV_TAG, sourceInventory)
			}
			else {
				putString(SOURCE_INV_TAG, lootTable)
			}
			
			put(PLAYER_INV_TAG, TagCompound().also {
				for((uuid, inventory) in playerInventories) {
					it.saveInventory(uuid.toString(), inventory)
				}
			})
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == STORAGE) {
			if (hasInventory(SOURCE_INV_TAG)) {
				loadInventory(SOURCE_INV_TAG, sourceInventory)
			}
			else {
				sourceLootTable = getStringOrNull(SOURCE_INV_TAG)
			}
			
			with(getCompound(PLAYER_INV_TAG)) {
				for(key in keySet()) {
					val uuid = try {
						UUID.fromString(key)
					} catch(e: Exception) {
						HEE.log.error("[TileEntityLootChest] could not parse UUID: $key")
						continue
					}
					
					playerInventories[uuid] = createInventory().also { loadInventory(key, it) }
				}
			}
		}
	}
}
