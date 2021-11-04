package chylex.hee.game.block.entity.base

import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.util.CHEST_FACING
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.world.util.FLAG_SKIP_RENDER
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.getTile
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getStringOrNull
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.tileentity.IChestLid
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.INameable
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.SoundEvents
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT, _interface = IChestLid::class)
abstract class TileEntityBaseChest(type: TileEntityType<out TileEntityBaseChest>) : TileEntityBase(type), IChestLid, ITickableTileEntity, INamedContainerProvider, INameable {
	private companion object {
		private const val CUSTOM_NAME_TAG = "CustomName"
		private const val VIEWER_COUNT_TAG = "ViewerCount"
	}
	
	val facing: Direction
		get() = world?.let { pos.getState(it)[CHEST_FACING] } ?: UP
	
	val isLidClosed: Boolean
		get() = viewerCount == 0
	
	protected abstract val defaultName: ITextComponent
	protected open val soundOpening: SoundEvent = SoundEvents.BLOCK_CHEST_OPEN
	protected open val soundClosing: SoundEvent = SoundEvents.BLOCK_CHEST_CLOSE
	
	private val lidAngle = LerpedFloat(0F)
	private var viewerCount by Notifying(0, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
	private var customName: ITextComponent? = null
	
	// Animation
	
	@Sided(Side.CLIENT)
	override fun getLidAngle(partialTicks: Float): Float {
		return lidAngle.get(partialTicks)
	}
	
	final override fun tick() {
		val currentLidAngle = lidAngle.currentValue
		var newLidAngle = currentLidAngle
		
		if (viewerCount > 0 && currentLidAngle < 1F) {
			newLidAngle = (currentLidAngle + 0.1F).coerceAtMost(1F)
			
			if (currentLidAngle == 0F) {
				playChestSound(soundOpening)
			}
		}
		else if (viewerCount == 0 && currentLidAngle > 0F) {
			newLidAngle = (currentLidAngle - 0.1F).coerceAtLeast(0F)
			
			if (currentLidAngle >= 0.5F && newLidAngle < 0.5F) {
				playChestSound(soundClosing)
			}
		}
		
		lidAngle.update(newLidAngle)
	}
	
	protected open fun playChestSound(sound: SoundEvent) {
		sound.playServer(wrld, pos, SoundCategory.BLOCKS, volume = 0.5F, pitch = wrld.rand.nextFloat(0.9F, 1.0F))
	}
	
	// Inventory handling
	
	fun isUsableByPlayer(player: PlayerEntity): Boolean {
		return pos.getTile<TileEntityBaseChest>(wrld) === this && pos.distanceSqTo(player) <= 64
	}
	
	protected fun createChestInventory(wrapped: IInventory): IInventory {
		return object : IInventory by wrapped {
			override fun openInventory(player: PlayerEntity) {
				wrapped.openInventory(player)
				++viewerCount
			}
			
			override fun closeInventory(player: PlayerEntity) {
				wrapped.closeInventory(player)
				--viewerCount
			}
			
			override fun markDirty() {
				wrapped.markDirty()
				this@TileEntityBaseChest.markDirty()
			}
			
			override fun isUsableByPlayer(player: PlayerEntity): Boolean {
				return this@TileEntityBaseChest.isUsableByPlayer(player)
			}
		}
	}
	
	// Custom name
	
	final override fun hasCustomName(): Boolean {
		return customName != null
	}
	
	final override fun getName(): ITextComponent {
		return customName ?: defaultName
	}
	
	final override fun getCustomName(): ITextComponent? {
		return customName
	}
	
	final override fun getDisplayName(): ITextComponent {
		return name
	}
	
	fun setCustomName(customName: ITextComponent) {
		this.customName = customName
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		customName?.let {
			putString(CUSTOM_NAME_TAG, ITextComponent.Serializer.toJson(it))
		}
		
		if (context == NETWORK) {
			putShort(VIEWER_COUNT_TAG, viewerCount.toShort())
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		customName = getStringOrNull(CUSTOM_NAME_TAG)?.let(ITextComponent.Serializer::getComponentFromJson)
		
		if (context == NETWORK) {
			viewerCount = getShort(VIEWER_COUNT_TAG).toInt()
		}
	}
}
