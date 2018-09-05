package chylex.hee.render.block
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

@SideOnly(Side.CLIENT)
object RenderTileEndPortal : RenderTileAbstractPortal<TileEntityEndPortal>(){
	private const val seed = 31100L
	private val rand = Random(seed)
	
	override fun generateNextColor(tile: TileEntityEndPortal){
		color[0] = (rand.nextFloat() * 0.5F) + 0.1F
		color[1] = (rand.nextFloat() * 0.5F) + 0.4F
		color[2] = (rand.nextFloat() * 0.5F) + 0.5F
	}
	
	override fun render(tile: TileEntityEndPortal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		rand.setSeed(seed)
		super.render(tile, x, y, z, partialTicks, destroyStage, alpha)
	}
}
