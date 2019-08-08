package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Animating
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Invisible
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Visible
import chylex.hee.system.util.math.LerpedFloat
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import kotlin.math.max
import kotlin.math.min

abstract class TileEntityBasePortalController : TileEntityBase(), IPortalController, ITickable{
	private companion object{
		private const val RENDER_STATE_TAG = "RenderState"
		private const val RENDER_PROGRESS_TAG = "RenderProgress"
	}
	
	sealed class ForegroundRenderState(val progress: Float){
		class Animating(progress: Float): ForegroundRenderState(progress)
		object Visible : ForegroundRenderState(1F)
		object Invisible : ForegroundRenderState(0F)
	}
	
	protected abstract val serverRenderState: ForegroundRenderState
	protected abstract val clientAnimationFadeInSpeed: Float
	protected abstract val clientAnimationFadeOutSpeed: Float
	
	private var clientRenderState: ForegroundRenderState = Invisible
	override val clientAnimationProgress = LerpedFloat(0F)
	override val clientPortalOffset = LerpedFloat(0F)
	
	private fun updateAnimation(){
		when(clientRenderState){
			Invisible    -> clientAnimationProgress.update(max(0F, clientAnimationProgress - clientAnimationFadeOutSpeed))
			is Animating -> clientAnimationProgress.update(min(1F, clientAnimationProgress + clientAnimationFadeInSpeed))
		}
	}
	
	override fun update(){
		if (world.isRemote){
			updateAnimation()
		}
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == NETWORK){
			setString(RENDER_STATE_TAG, when(val state = serverRenderState){
				is Animating -> "Animating".also { setFloat(RENDER_PROGRESS_TAG, state.progress) }
				Visible      -> "Visible"
				Invisible    -> ""
			})
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == NETWORK){
			clientRenderState = when(getString(RENDER_STATE_TAG)){
				"Animating" -> Animating(getFloat(RENDER_PROGRESS_TAG))
				"Visible"   -> Visible
				else        -> Invisible
			}
			
			if (clientRenderState != Invisible){
				clientAnimationProgress.updateImmediately(clientRenderState.progress)
			}
		}
	}
}
