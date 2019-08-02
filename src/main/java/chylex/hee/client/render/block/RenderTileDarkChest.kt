package chylex.hee.client.render.block
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.system.Resource
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileDarkChest : TileEntityChestRenderer(){
	private val TEX_SINGLE = Resource.Custom("textures/entity/dark_chest_single.png")
	private val TEX_DOUBLE = Resource.Custom("textures/entity/dark_chest_double.png")
	
	init{
		isChristmas = false
	}
	
	override fun render(tile: TileEntityChest, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		val prevTexSingle = TEXTURE_NORMAL
		val prevTexDouble = TEXTURE_NORMAL_DOUBLE
		
		TEXTURE_NORMAL = TEX_SINGLE
		TEXTURE_NORMAL_DOUBLE = TEX_DOUBLE
		
		super.render(tile, x, y, z, partialTicks, destroyStage, alpha)
		
		TEXTURE_NORMAL = prevTexSingle
		TEXTURE_NORMAL_DOUBLE = prevTexDouble
	}
	
	object AsItem : TileEntityItemStackRenderer(){
		private val tile = TileEntityDarkChest()
		
		override fun renderByItem(stack: ItemStack, partialTicks: Float){
			TileEntityRendererDispatcher.instance.render(tile, 0.0, 0.0, 0.0, partialTicks)
		}
	}
}
