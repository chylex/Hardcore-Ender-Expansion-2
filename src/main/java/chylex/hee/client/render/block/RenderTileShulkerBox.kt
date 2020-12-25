package chylex.hee.client.render.block

import net.minecraft.client.renderer.entity.model.ShulkerModel
import net.minecraft.client.renderer.tileentity.ShulkerBoxTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.monster.ShulkerEntity

class RenderTileShulkerBox(dispatcher: TileEntityRendererDispatcher) : ShulkerBoxTileEntityRenderer(ShulkerModel<ShulkerEntity>(), dispatcher)
