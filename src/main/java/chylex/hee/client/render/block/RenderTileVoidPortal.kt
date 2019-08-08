package chylex.hee.client.render.block
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockVoidPortalInner.Companion.TYPE
import chylex.hee.game.block.BlockVoidPortalInner.IVoidPortalController
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.math.LerpedFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileVoidPortal : RenderTileAbstractPortal<TileEntityPortalInner.Void, IVoidPortalController>(){
	private object ActiveReturnController : IVoidPortalController{
		override val currentInstance = THE_HUB_INSTANCE
		override val clientAnimationProgress = LerpedFloat(1F)
		override val clientPortalOffset = LerpedFloat(0F)
	}
	
	override fun findController(world: World, pos: BlockPos) = when(pos.getState(world).takeIf { it.block === ModBlocks.VOID_PORTAL_INNER }?.get(TYPE)){
		HUB -> pos.closestTickingTile<TileEntityVoidPortalStorage>(world, BlockAbstractPortal.MAX_DISTANCE_FROM_FRAME)
		RETURN_ACTIVE -> ActiveReturnController
		else -> null
	}
	
	override fun generateSeed(controller: IVoidPortalController): Long{
		return controller.currentInstance?.territory?.desc?.colors?.portalSeed ?: 0L
	}
	
	override fun generateNextColor(controller: IVoidPortalController, layer: Int){
		controller.currentInstance?.territory?.desc?.colors?.nextPortalColor(rand, color)
	}
}
