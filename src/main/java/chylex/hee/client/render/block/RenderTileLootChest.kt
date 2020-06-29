package chylex.hee.client.render.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
class RenderTileLootChest(dispatcher: TileEntityRendererDispatcher) : RenderTileAbstractChest<TileEntityLootChest>(dispatcher, TEX){
	companion object{
		val TEX = Resource.Custom("entity/loot_chest")
	}
	
	object AsItem : ItemStackTileEntityRenderer(){
		private val tile = TileEntityLootChest()
		
		override fun render(stack: ItemStack, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
			TileEntityRendererDispatcher.instance.renderItem(tile, matrix, buffer, combinedLight, combinedOverlay)
		}
	}
}
