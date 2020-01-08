package chylex.hee.client.render.block
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
object RenderTileLootChest : RenderTileAbstractChest<TileEntityLootChest>(){
	override val texture = Resource.Custom("textures/entity/loot_chest.png")
	
	object AsItem : ItemStackTileEntityRenderer(){
		private val tile = TileEntityLootChest()
		
		override fun renderByItem(stack: ItemStack){
			TileEntityRendererDispatcher.instance.render(tile, 0.0, 0.0, 0.0, MC.partialTicks)
		}
	}
}
