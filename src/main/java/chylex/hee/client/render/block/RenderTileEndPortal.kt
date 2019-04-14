package chylex.hee.client.render.block
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.system.util.closestTickingTile
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileEndPortal : RenderTileAbstractPortal<TileEntityPortalInner.End, TileEntityEndPortalAcceptor>(){
	override fun findController(world: World, pos: BlockPos): TileEntityEndPortalAcceptor?{
		return pos.closestTickingTile(world, BlockAbstractPortal.MAX_DISTANCE_FROM_FRAME)
	}
	
	override fun generateSeed(controller: TileEntityEndPortalAcceptor): Long{
		return 31100L
	}
	
	override fun generateNextColor(controller: TileEntityEndPortalAcceptor, layer: Int){
		color[0] = (rand.nextFloat() * 0.5F) + 0.1F
		color[1] = (rand.nextFloat() * 0.5F) + 0.4F
		color[2] = (rand.nextFloat() * 0.5F) + 0.5F
	}
}
