package chylex.hee.game.block.entity.base

import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.base.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.tileentity.TileEntityType
import kotlin.math.max
import kotlin.math.min

abstract class TileEntityBasePortalController(type: TileEntityType<out TileEntityBasePortalController>) : TileEntityBaseSpecialFirstTick(type), IPortalController {
	private companion object {
		private const val RENDER_STATE_TAG = "RenderState"
		private const val RENDER_PROGRESS_TAG = "RenderProgress"
	}
	
	sealed class ForegroundRenderState(val progress: Float) {
		class Animating(progress: Float) : ForegroundRenderState(progress)
		object Visible : ForegroundRenderState(1F)
		object Invisible : ForegroundRenderState(0F)
	}
	
	protected abstract val serverRenderState: ForegroundRenderState
	protected abstract val clientAnimationFadeInSpeed: Float
	protected abstract val clientAnimationFadeOutSpeed: Float
	
	private var clientRenderState: ForegroundRenderState = Invisible
	override val clientAnimationProgress = LerpedFloat(0F)
	override val clientPortalOffset = LerpedFloat(0F)
	
	private fun updateAnimation() {
		when (clientRenderState) {
			Invisible    -> clientAnimationProgress.update(max(0F, clientAnimationProgress - clientAnimationFadeOutSpeed))
			is Animating -> clientAnimationProgress.update(min(1F, clientAnimationProgress + clientAnimationFadeInSpeed))
			else         -> {}
		}
	}
	
	override fun tick() {
		super.tick()
		
		if (wrld.isRemote) {
			updateAnimation()
		}
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == NETWORK) {
			putString(RENDER_STATE_TAG, when (val state = serverRenderState) {
				is Animating -> "Animating".also { putFloat(RENDER_PROGRESS_TAG, state.progress) }
				Visible      -> "Visible"
				Invisible    -> ""
			})
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == NETWORK) {
			clientRenderState = when (getString(RENDER_STATE_TAG)) {
				"Animating" -> Animating(getFloat(RENDER_PROGRESS_TAG))
				"Visible"   -> Visible
				else        -> Invisible
			}
			
			if (clientRenderState != Invisible) {
				clientAnimationProgress.updateImmediately(clientRenderState.progress)
			}
		}
	}
}
