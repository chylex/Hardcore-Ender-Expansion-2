package chylex.hee.client.render.block
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.math.LerpedFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileEndPortal : RenderTileAbstractPortal<TileEntityPortalInner.End, IPortalController>(){
	private object AlwaysOnController : IPortalController{
		override val clientAnimationProgress = LerpedFloat(1F)
		override val clientPortalOffset = LerpedFloat(0F)
	}
	
	override fun findController(world: World, pos: BlockPos): IPortalController?{
		if (world.provider.dimension == 1){
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
