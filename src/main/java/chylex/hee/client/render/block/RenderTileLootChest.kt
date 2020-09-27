package chylex.hee.client.render.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.init.ModAtlases
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.state.properties.ChestType

@Sided(Side.CLIENT)
class RenderTileLootChest(dispatcher: TileEntityRendererDispatcher) : ChestTileEntityRenderer<TileEntityLootChest>(dispatcher){
	companion object{
		val TEX = Resource.Custom("entity/loot_chest")
		private val MAT = Material(ModAtlases.ATLAS_TILES, TEX)
	}
	
	init{
		isChristmas = false
	}
	
	override fun getMaterial(tile: TileEntityLootChest, type: ChestType): Material{
		return MAT
	}
}
