package chylex.hee.client.render.block
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
object RenderTileDarkChest : ChestTileEntityRenderer<TileEntityDarkChest>(){
	private val TEX_SINGLE = Resource.Custom("textures/entity/dark_chest_single.png")
	private val TEX_DOUBLE = Resource.Custom("textures/entity/dark_chest_double.png")
	
	init{
		isChristmas = false
	}
	
	// UPDATE replace
	
	override fun render(tile: TileEntityDarkChest, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		val prevTexSingle = TEXTURE_NORMAL
		val prevTexDouble = TEXTURE_NORMAL_DOUBLE
		
		TEXTURE_NORMAL = TEX_SINGLE
		TEXTURE_NORMAL_DOUBLE = TEX_DOUBLE
		
		super.render(tile, x, y, z, partialTicks, destroyStage)
		
		TEXTURE_NORMAL = prevTexSingle
		TEXTURE_NORMAL_DOUBLE = prevTexDouble
	}
	
	object AsItem : ItemStackTileEntityRenderer(){
		private val tile = TileEntityDarkChest()
		
		override fun renderByItem(stack: ItemStack){
			TileEntityRendererDispatcher.instance.renderAsItem(tile)
		}
	}
}
