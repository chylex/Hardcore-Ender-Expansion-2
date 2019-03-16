package chylex.hee.client.render.block
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.square
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.pow

@SideOnly(Side.CLIENT)
object RenderTileEndPortal : RenderTileAbstractPortal<TileEntityEndPortal>(){
	private const val seed = 31100L
	private val rand = Random(seed)
	
	private var isAnimating = false
	private var animationProgress = 0F
	
	override fun generateNextColor(layer: Int){
		val easingMp = if (isAnimating)
			(1.1F - square(animationProgress * 4.5F - 4.816F) + 22.1F * (1F - ((layer - 1F) / 14F).pow(1.2F))).coerceIn(0F, 1F).pow(1.5F)
		else
			animationProgress
		
		color[0] = ((rand.nextFloat() * 0.5F) + 0.1F) * easingMp
		color[1] = ((rand.nextFloat() * 0.5F) + 0.4F) * easingMp
		color[2] = ((rand.nextFloat() * 0.5F) + 0.5F) * easingMp
	}
	
	override fun render(tile: TileEntityEndPortal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		val acceptor = tile.pos.closestTickingTile<TileEntityEndPortalAcceptor>(tile.world, BlockAbstractPortal.MAX_DISTANCE_FROM_FRAME)
		
		animationProgress = acceptor?.foregroundRenderProgress?.get(partialTicks) ?: 0F
		isAnimating = animationProgress > 0F && animationProgress < 1F
		
		rand.setSeed(seed)
		super.render(tile, x, y, z, partialTicks, destroyStage, alpha)
	}
}
