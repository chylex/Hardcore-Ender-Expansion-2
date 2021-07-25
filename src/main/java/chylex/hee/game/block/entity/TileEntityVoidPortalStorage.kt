package chylex.hee.game.block.entity

import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockVoidPortalInner.ITerritoryInstanceFactory
import chylex.hee.game.block.BlockVoidPortalInner.IVoidPortalController
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBasePortalController
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.game.inventory.util.ItemStackHandlerInventory
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.storage.PlayerTokenStorage
import chylex.hee.game.world.server.SpawnInfo
import chylex.hee.game.world.util.FLAG_SKIP_RENDER
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.isAnyPlayerWithinRange
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.ModTileEntities
import chylex.hee.util.math.square
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import kotlin.math.min
import kotlin.math.nextUp

class TileEntityVoidPortalStorage(type: TileEntityType<TileEntityVoidPortalStorage>) : TileEntityBasePortalController(type), IVoidPortalController, INamedContainerProvider {
	constructor() : this(ModTileEntities.VOID_PORTAL_STORAGE)
	
	object Type : IHeeTileEntityType<TileEntityVoidPortalStorage> {
		override val blocks
			get() = arrayOf(ModBlocks.VOID_PORTAL_STORAGE, ModBlocks.VOID_PORTAL_STORAGE_CRAFTED)
	}
	
	private companion object {
		private const val ACTIVATION_DURATION_TICKS = 20 * 10
		
		private const val FADE_IN_PROGRESS_PER_UPDATE = 0.04F
		private const val FADE_OUT_PROGRESS_PER_UPDATE = 0.02F
		private const val FADE_IN_DURATION_TICKS = 1F / FADE_IN_PROGRESS_PER_UPDATE
		
		private const val SLOWING_DURATION_TICKS = 75
		private const val SLOWING_EXTRA_DELAY_TICKS = 4
		private val SLOWING_PROGRESS_PER_UPDATE = ((1000F / 20F) * BlockAbstractPortal.TRANSLATION_SPEED_INV).toFloat().nextUp()
		
		private const val TERRITORY_TAG = "Territory"
	}
	
	private class TerritoryInstanceFactory(override val territory: TerritoryType, private val stack: ItemStack) : ITerritoryInstanceFactory {
		override fun create(entity: Entity): TerritoryInstance? {
			return ModItems.PORTAL_TOKEN.getOrCreateTerritoryInstance(stack, entity)
		}
	}
	
	// Client animation
	
	override val serverRenderState
		get() = when (currentInstanceFactory) {
			null -> Invisible
			else ->
				if (remainingTime > ACTIVATION_DURATION_TICKS - FADE_IN_DURATION_TICKS)
					Animating((ACTIVATION_DURATION_TICKS - remainingTime) / FADE_IN_DURATION_TICKS)
				else
					Visible
		}
	
	override val clientAnimationFadeInSpeed
		get() = FADE_IN_PROGRESS_PER_UPDATE
	
	override val clientAnimationFadeOutSpeed
		get() = FADE_OUT_PROGRESS_PER_UPDATE
	
	// Token handling
	
	override var currentInstanceFactory: ITerritoryInstanceFactory? by Notifying(null, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
		private set
	
	private val firstSpawnInfo = HashMap<TerritoryInstance, SpawnInfo>(1, 1F)
	
	private var remainingTime = 0
	private var clientTime = 0
	
	fun activateToken(stack: ItemStack) {
		currentInstanceFactory = ItemPortalToken.getTerritoryType(stack)?.let { TerritoryInstanceFactory(it, stack) }
		remainingTime = ACTIVATION_DURATION_TICKS
	}
	
	fun prepareSpawnPoint(entity: Entity): SpawnInfo? {
		return currentInstanceFactory?.create(entity)?.let { firstSpawnInfo.getOrPut(it) { it.prepareSpawnPoint(entity, clearanceRadius = 1) } }
	}
	
	// Container
	
	override fun getDisplayName(): ITextComponent {
		return TranslationTextComponent("gui.hee.portal_token_storage.title")
	}
	
	override fun createMenu(id: Int, inventory: PlayerInventory, player: PlayerEntity): Container {
		return ContainerPortalTokenStorage(id, player, ItemStackHandlerInventory(PlayerTokenStorage.forPlayer(player)), this)
	}
	
	// Overrides
	
	override fun tick() {
		super.tick()
		
		if (!wrld.isRemote) {
			if ((remainingTime > 0 && --remainingTime == 0) || !pos.isAnyPlayerWithinRange(wrld, 160)) {
				currentInstanceFactory = null
				remainingTime = 0
				firstSpawnInfo.clear()
			}
		}
		else {
			if (clientAnimationProgress.currentValue > 0F) { // just a small effect so don't care about syncing with server
				val slowingStartTime = ACTIVATION_DURATION_TICKS - SLOWING_DURATION_TICKS - SLOWING_EXTRA_DELAY_TICKS
				
				if (++clientTime > slowingStartTime) {
					val progress = min(1F, (clientTime - slowingStartTime).toFloat() / SLOWING_DURATION_TICKS)
					val offset = SLOWING_PROGRESS_PER_UPDATE * square(progress)
					
					clientPortalOffset.update(clientPortalOffset.currentValue + offset)
				}
			}
			else {
				clientTime = 0
				clientPortalOffset.updateImmediately(0F)
			}
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == NETWORK) {
			currentInstanceFactory?.let { putEnum(TERRITORY_TAG, it.territory) }
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == NETWORK) {
			currentInstanceFactory = getEnum<TerritoryType>(TERRITORY_TAG)?.let {
				object : ITerritoryInstanceFactory {
					override val territory = it
					override fun create(entity: Entity): TerritoryInstance? = null
				}
			} ?: currentInstanceFactory // keep previous instance for animation
		}
	}
}
