package chylex.hee.client.gui
import chylex.hee.client.MC
import chylex.hee.client.render.gl.GL
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.world.totalTime
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.gui.screen.inventory.BrewingStandScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.BrewingStandContainer
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiBrewingStandCustom(container: BrewingStandContainer, inventory: PlayerInventory, title: ITextComponent) : BrewingStandScreen(container, inventory, title){
	private companion object{
		private val TEX_BACKGROUND = Resource.Custom("textures/gui/brewing_stand.png")
		private val BUBBLE_LENGTHS = intArrayOf(0, 6, 11, 16, 20, 24, 29)
	}
	
	private var brewStartTime = MC.world!!.totalTime
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		
		GL.color(1F, 1F, 1F, 1F)
		GL.bindTexture(TEX_BACKGROUND)
		blit(x, y, 0, 0, xSize, ySize)
		
		val worldTime = MC.world!!.totalTime
		val brewTime = container.func_216981_f() // RENAME getBrewTime
		
		if (brewTime > 0){
			val brewProgress = (28F * (1F - (brewTime / 400F))).toInt()
			
			if (brewProgress > 0){
				blit(x + 97, y + 16, 176, 0, 9, brewProgress)
			}
			
			val bubbleLength = BUBBLE_LENGTHS[((worldTime - brewStartTime).toInt() / 2) % 7]
			
			if (bubbleLength > 0){
				blit(x + 63, y + 43 - bubbleLength, 185, 29 - bubbleLength, 12, bubbleLength)
			}
		}
		else{
			brewStartTime = worldTime
		}
		
		if (container.getSlot(TileEntityBrewingStandCustom.SLOT_MODIFIER).hasStack){
			blit(x + 62, y + 45, 197, 0, 14, 2)
		}
		
		for(slotIndex in TileEntityBrewingStandCustom.SLOTS_POTIONS){
			val slot = container.getSlot(slotIndex)
			
			if (!slot.hasStack){
				blit(x + slot.xPos, y + slot.yPos, 211, 0, 16, 16)
			}
		}
	}
}
