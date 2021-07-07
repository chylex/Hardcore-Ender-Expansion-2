package chylex.hee.client.render.block

import chylex.hee.game.Resource
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.init.ModAtlases
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.model.RenderMaterial
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.state.properties.ChestType

@Sided(Side.CLIENT)
class RenderTileLootChest(dispatcher: TileEntityRendererDispatcher) : ChestTileEntityRenderer<TileEntityLootChest>(dispatcher) {
	companion object {
		val TEX = Resource.Custom("entity/loot_chest")
		private val MAT = RenderMaterial(ModAtlases.ATLAS_TILES, TEX)
	}
	
	override fun getMaterial(tile: TileEntityLootChest, type: ChestType): RenderMaterial {
		return MAT
	}
}
