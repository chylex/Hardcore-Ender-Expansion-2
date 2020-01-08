package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockVoidPortalInner.IVoidPortalController
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBasePortalController
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.init.ModItems
import chylex.hee.init.ModTileEntities
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.isAnyPlayerWithinRange
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import kotlin.math.min
import kotlin.math.nextUp

class TileEntityVoidPortalStorage(type: TileEntityType<TileEntityVoidPortalStorage>) : TileEntityBasePortalController(type), IVoidPortalController{
	constructor() : this(ModTileEntities.VOID_PORTAL_STORAGE)
	
	private companion object{
		private const val ACTIVATION_DURATION_TICKS = 20 * 10
		
		private const val FADE_IN_PROGRESS_PER_UPDATE = 0.04F
		private const val FADE_OUT_PROGRESS_PER_UPDATE = 0.02F
		private const val FADE_IN_DURATION_TICKS = 1F / FADE_IN_PROGRESS_PER_UPDATE
		
		private const val SLOWING_DURATION_TICKS = 75
		private const val SLOWING_EXTRA_DELAY_TICKS = 4
		private val SLOWING_PROGRESS_PER_UPDATE = ((1000F / 20F) / BlockAbstractPortal.TRANSLATION_SPEED).nextUp()
		
		private const val INSTANCE_TAG = "Instance"
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
	
	override val clientAnimationFadeOutSpeed
		get() = FADE_OUT_PROGRESS_PER_UPDATE
	
	// Token handling
	
	override var currentInstance: TerritoryInstance? by Notifying(null, FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
		private set
	
	private var firstSpawnInfo: SpawnInfo? = null
	
	private var remainingTime = 0
	private var clientTime = 0
	
	fun activateToken(stack: ItemStack){
		currentInstance = ModItems.PORTAL_TOKEN.getOrCreateTerritoryInstance(stack)
		remainingTime = ACTIVATION_DURATION_TICKS
	}
	
	fun prepareSpawnPoint(entity: Entity): SpawnInfo?{
		return firstSpawnInfo ?: currentInstance?.prepareSpawnPoint(entity, clearanceRadius = 1).also { firstSpawnInfo = it }
	}
	
	// Overrides
	
	override fun tick(){
		super.tick()
		
		if (!wrld.isRemote){
			if ((remainingTime > 0 && --remainingTime == 0) || !pos.isAnyPlayerWithinRange(wrld, 160)){
				currentInstance = null
				firstSpawnInfo = null
				remainingTime = 0
			}
		}
		else{
			if (clientAnimationProgress.currentValue > 0F){ // just a small effect so don't care about syncing with server
				val slowingStartTime = ACTIVATION_DURATION_TICKS - SLOWING_DURATION_TICKS - SLOWING_EXTRA_DELAY_TICKS
				
				if (++clientTime > slowingStartTime){
					val progress = min(1F, (clientTime - slowingStartTime).toFloat() / SLOWING_DURATION_TICKS)
					val offset = SLOWING_PROGRESS_PER_UPDATE * square(progress)
					
					clientPortalOffset.update(clientPortalOffset.currentValue + offset)
				}
			}
			else{
				clientTime = 0
				clientPortalOffset.updateImmediately(0F)
			}
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == NETWORK){
			currentInstance?.let { putInt(INSTANCE_TAG, it.hash) }
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == NETWORK){
			currentInstance = getIntegerOrNull(INSTANCE_TAG)?.let(TerritoryInstance.Companion::fromHash) ?: currentInstance // keep previous instance for animation
		}
	}
}
