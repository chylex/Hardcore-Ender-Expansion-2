package chylex.hee.client.render.block
import chylex.hee.HEE
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.world.closestTickingTile
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.LerpedFloat
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Sided(Side.CLIENT)
class RenderTileEndPortal(dispatcher: TileEntityRendererDispatcher) : RenderTileAbstractPortal<TileEntityPortalInner.End, IPortalController>(dispatcher){
	private object AlwaysOnController : IPortalController{
		override val clientAnimationProgress = LerpedFloat(1F)
		override val clientPortalOffset = LerpedFloat(0F)
	}
	
	override fun findController(world: World, pos: BlockPos): IPortalController?{
		if (world.dimension.type === HEE.dim){
			return AlwaysOnController
		}
		
		return pos.closestTickingTile<TileEntityEndPortalAcceptor>(world, BlockAbstractPortal.MAX_DISTANCE_FROM_FRAME)
	}
	
	override fun generateSeed(controller: IPortalController): Long{
		return 31100L
	}
	
	override fun generateNextColor(controller: IPortalController, layer: Int){
		color[0] = (rand.nextFloat() * 0.5F) + 0.1F
		color[1] = (rand.nextFloat() * 0.5F) + 0.4F
		color[2] = (rand.nextFloat() * 0.5F) + 0.5F
	}
}
