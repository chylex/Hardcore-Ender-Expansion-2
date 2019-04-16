package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockVoidPortalInner.IVoidPortalController
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.init.ModItems
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getIntegerOrNull
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class TileEntityVoidPortalStorage : TileEntityBasePortalController(), IVoidPortalController{
	private companion object{
		private const val ACTIVATION_DURATION_TICKS = 20 * 10
		
		private const val FADE_IN_PROGRESS_PER_UPDATE = 0.05F
		private const val FADE_OUT_PROGRESS_PER_UPDATE = 0.02F
		
		private const val FADE_IN_DURATION_TICKS = 1F / FADE_IN_PROGRESS_PER_UPDATE
	}
	
	// Client animation
	
	override val serverRenderState
		get() = when(currentInstance){
			null -> Invisible
			else ->
				if (remainingTime > ACTIVATION_DURATION_TICKS - FADE_IN_DURATION_TICKS)
					Animating((ACTIVATION_DURATION_TICKS - remainingTime) / FADE_IN_DURATION_TICKS)
				else
					Visible
		}
	
	override val clientAnimationFadeInSpeed
		get() = FADE_IN_PROGRESS_PER_UPDATE
	
	override val clientAnimationFadeOutSpeed // TODO add slow down animation
		get() = FADE_OUT_PROGRESS_PER_UPDATE
	
	// Token handling
	
	override var currentInstance: TerritoryInstance? by Notifying(null, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
		private set
	
	private var remainingTime = 0
	
	fun activateToken(stack: ItemStack){
		currentInstance = ModItems.PORTAL_TOKEN.getOrCreateTerritoryInstance(stack)
		remainingTime = ACTIVATION_DURATION_TICKS
	}
	
	// Overrides
	
	override fun update(){
		super.update()
		
		if (!world.isRemote && remainingTime > 0 && --remainingTime == 0){
			currentInstance = null
		}
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		super.writeNBT(nbt, context)
		
		if (context == NETWORK){
			currentInstance?.let { setInteger("Instance", it.hash) }
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		super.readNBT(nbt, context)
		
		if (context == NETWORK){
			currentInstance = getIntegerOrNull("Instance")?.let(TerritoryInstance.Companion::fromHash) ?: currentInstance // keep previous instance for animation
		}
	}
}
