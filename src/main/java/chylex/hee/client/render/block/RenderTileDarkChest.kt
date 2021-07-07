package chylex.hee.client.render.block

import chylex.hee.game.Resource
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.init.ModAtlases
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.model.RenderMaterial
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.state.properties.ChestType
import net.minecraft.state.properties.ChestType.LEFT
import net.minecraft.state.properties.ChestType.RIGHT

@Sided(Side.CLIENT)
class RenderTileDarkChest(dispatcher: TileEntityRendererDispatcher) : ChestTileEntityRenderer<TileEntityDarkChest>(dispatcher) {
	companion object {
		val TEX_SINGLE = Resource.Custom("entity/dark_chest_single")
		val TEX_DOUBLE_LEFT = Resource.Custom("entity/dark_chest_left")
		val TEX_DOUBLE_RIGHT = Resource.Custom("entity/dark_chest_right")
		
		private val MAT_SINGLE = RenderMaterial(ModAtlases.ATLAS_TILES, TEX_SINGLE)
		private val MAT_DOUBLE_LEFT = RenderMaterial(ModAtlases.ATLAS_TILES, TEX_DOUBLE_LEFT)
		private val MAT_DOUBLE_RIGHT = RenderMaterial(ModAtlases.ATLAS_TILES, TEX_DOUBLE_RIGHT)
	}
	
	override fun getMaterial(tile: TileEntityDarkChest, type: ChestType) = when (type) {
		LEFT  -> MAT_DOUBLE_LEFT
		RIGHT -> MAT_DOUBLE_RIGHT
		else  -> MAT_SINGLE
	}
}
