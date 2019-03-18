package chylex.hee.client.render.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.system.Resource
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileLootChest : RenderTileAbstractChest<TileEntityLootChest>(){
	override val texture = Resource.Custom("textures/entity/loot_chest.png")
	
	object AsItem : TileEntityItemStackRenderer(){
		private val tile = TileEntityLootChest()
		
		override fun renderByItem(stack: ItemStack, partialTicks: Float){
			TileEntityRendererDispatcher.instance.render(tile, 0.0, 0.0, 0.0, partialTicks)
		}
	}
}