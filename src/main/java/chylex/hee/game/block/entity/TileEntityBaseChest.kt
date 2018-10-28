package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.render.util.LerpedFloat
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.IInventory
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.ITickable
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.IWorldNameable
import net.minecraft.world.World

abstract class TileEntityBaseChest : TileEntityBase(), ITickable, IWorldNameable{
	val facing: EnumFacing
		get() = world?.let { pos.getState(it).getValue(FACING) } ?: UP
	
	val lidAngle = LerpedFloat(0F)
	
	protected abstract val defaultName: String
	protected open val soundOpening: SoundEvent = SoundEvents.BLOCK_CHEST_OPEN
	protected open val soundClosing: SoundEvent = SoundEvents.BLOCK_CHEST_CLOSE
	
	private var viewerCount by Notifying(0, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
	private var customName: String? = null
	
	// Animation
	
	final override fun update(){
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
		world.playSound(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, sound, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat(0.9F, 1.0F))
	}
	
	// Inventory handling
	
	protected abstract fun getInventoryFor(player: EntityPlayer): IInventory
	
	fun isUsableByPlayer(player: EntityPlayer): Boolean{
		return pos.getTile<TileEntityBaseChest>(world) === this && pos.distanceSqTo(player) <= 64
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
	
	final override fun getName(): String{
		return customName ?: defaultName
	}
	
	final override fun getDisplayName(): ITextComponent{
		return customName?.let(::TextComponentString) ?: TextComponentTranslation(defaultName)
	}
	
	fun setCustomName(customName: String){
		this.customName = customName
	}
	
	// Serialization
	
	override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean{
		return newState.block != oldState.block
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		customName?.let {
			setString("CustomName", it)
		}
		
		if (context == NETWORK){
			setShort("ViewerCount", viewerCount.toShort())
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (hasKey("CustomName")){
			customName = getString("CustomName")
		}
		
		if (context == NETWORK){
			viewerCount = getShort("ViewerCount").toInt()
		}
	}
}
