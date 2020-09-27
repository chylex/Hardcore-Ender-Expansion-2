package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.client.render.block.RenderTileDarkChest
import chylex.hee.client.render.block.RenderTileLootChest
import chylex.hee.game.container.slot.SlotTrinketItem
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraft.client.renderer.Atlases
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object ModAtlases{
	val ATLAS_GUIS: ResourceLocation = Atlases.CHEST_ATLAS
	val ATLAS_TILES: ResourceLocation = Atlases.CHEST_ATLAS
	
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		if (e.map.textureLocation == ATLAS_GUIS){
			e.addSprite(SlotTrinketItem.TEX_SLOT_OVERLAY)
		}
		
		if (e.map.textureLocation == ATLAS_TILES){
			e.addSprite(RenderTileDarkChest.TEX_SINGLE)
			e.addSprite(RenderTileDarkChest.TEX_DOUBLE_LEFT)
			e.addSprite(RenderTileDarkChest.TEX_DOUBLE_RIGHT)
			e.addSprite(RenderTileLootChest.TEX)
		}
	}
}
