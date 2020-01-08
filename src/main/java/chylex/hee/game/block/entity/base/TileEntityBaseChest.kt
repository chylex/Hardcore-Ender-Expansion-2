package chylex.hee.game.block.entity.base
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.getState
import chylex.hee.system.util.getStringOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.use
import net.minecraft.block.ChestBlock.FACING
import net.minecraft.inventory.IInventory
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.INameable
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.text.ITextComponent

abstract class TileEntityBaseChest(type: TileEntityType<out TileEntityBaseChest>) : TileEntityBase(type), ITickableTileEntity, INameable{
	private companion object{
		private const val CUSTOM_NAME_TAG = "CustomName"
		private const val VIEWER_COUNT_TAG = "ViewerCount"
	}
	
	val facing: Direction
		get() = world?.let { pos.getState(it)[FACING] } ?: UP
	
	val isLidClosed: Boolean
		get() = viewerCount == 0
	
	val lidAngle = LerpedFloat(0F)
	
	protected abstract val defaultName: ITextComponent
	protected open val soundOpening: SoundEvent = Sounds.BLOCK_CHEST_OPEN
	protected open val soundClosing: SoundEvent = Sounds.BLOCK_CHEST_CLOSE
	
	private var viewerCount by Notifying(0, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
	private var customName: ITextComponent? = null
	
	// Animation
	
	final override fun tick(){
		val currentLidAngle = lidAngle.currentValue
		var newLidAngle = currentLidAngle
		
		if (viewerCount > 0 && currentLidAngle < 1F){
			newLidAngle = (currentLidAngle + 0.1F).coerceAtMost(1F)
			
			if (currentLidAngle == 0F){
				playChestSound(soundOpening)
			}
		}
		else if (viewerCount == 0 && currentLidAngle > 0F){
			newLidAngle = (currentLidAngle - 0.1F).coerceAtLeast(0F)
			
			if (currentLidAngle >= 0.5F && newLidAngle < 0.5F){
				playChestSound(soundClosing)
			}
		}
		
		lidAngle.update(newLidAngle)
	}
	
	protected open fun playChestSound(sound: SoundEvent){
		sound.playServer(wrld, pos, SoundCategory.BLOCKS, volume = 0.5F, pitch = wrld.rand.nextFloat(0.9F, 1.0F))
	}
	
	// Inventory handling
	
	protected abstract fun getInventoryFor(player: EntityPlayer): IInventory
	
	fun isUsableByPlayer(player: EntityPlayer): Boolean{
		return pos.getTile<TileEntityBaseChest>(wrld) === this && pos.distanceSqTo(player) <= 64
	}
	
	fun getChestInventoryFor(player: EntityPlayer): IInventory{
		val wrapped = getInventoryFor(player)
		
		return object : IInventory by wrapped{
			override fun openInventory(player: EntityPlayer){
				wrapped.openInventory(player)
				++viewerCount
			}
			
			override fun closeInventory(player: EntityPlayer){
				wrapped.closeInventory(player)
				--viewerCount
			}
			
			override fun markDirty(){
				wrapped.markDirty()
				this@TileEntityBaseChest.markDirty()
			}
			
			override fun isUsableByPlayer(player: EntityPlayer): Boolean{
				return this@TileEntityBaseChest.isUsableByPlayer(player)
			}
		}
	}
	
	// Custom name
	
	final override fun hasCustomName(): Boolean{
		return customName != null
	}
	
	final override fun getName(): ITextComponent{
		return customName ?: defaultName
	}
	
	final override fun getCustomName(): ITextComponent?{
		return customName
	}
	
	fun setCustomName(customName: ITextComponent){
		this.customName = customName
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		customName?.let {
			putString(CUSTOM_NAME_TAG, ITextComponent.Serializer.toJson(it))
		}
		
		if (context == NETWORK){
			putShort(VIEWER_COUNT_TAG, viewerCount.toShort())
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		customName = getStringOrNull(CUSTOM_NAME_TAG)?.let(ITextComponent.Serializer::fromJson)
		
		if (context == NETWORK){
			viewerCount = getShort(VIEWER_COUNT_TAG).toInt()
		}
	}
}
