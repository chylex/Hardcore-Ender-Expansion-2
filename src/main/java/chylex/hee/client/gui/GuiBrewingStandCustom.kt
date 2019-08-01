package chylex.hee.client.gui
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.container.ContainerBrewingStandCustom
import chylex.hee.system.Resource
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import net.minecraft.client.gui.inventory.GuiBrewingStand
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.IInventory
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiBrewingStandCustom(inventory: InventoryPlayer, private val brewingStand: IInventory) : GuiBrewingStand(inventory, brewingStand){
	private companion object{
		private val TEX_BACKGROUND = Resource.Custom("textures/gui/brewing_stand.png")
		private val BUBBLE_LENGTHS = intArrayOf(29, 24, 20, 16, 11, 6, 0)
	}
	
	init{
		inventorySlots = ContainerBrewingStandCustom(inventory, brewingStand)
		// TODO could look better if the background sprites on slots disappeared after inserting items in them
	}
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		
		GL.color(1F, 1F, 1F, 1F)
		MC.textureManager.bindTexture(TEX_BACKGROUND)
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize)
		
		val brewTime = brewingStand.getField(0)
		
		if (brewTime > 0){
			val brewProgress = (28F * (1F - (brewTime / 400F))).toInt()
			
			if (brewProgress > 0){
				drawTexturedModalRect(x + 97, y + 16, 176, 0, 9, brewProgress)
			}
			
			val bubbleLength = BUBBLE_LENGTHS[brewTime / 2 % 7]
			
			if (bubbleLength > 0){
				drawTexturedModalRect(x + 63, y + 43 - bubbleLength, 185, 29 - bubbleLength, 12, bubbleLength)
			}
		}
		
		if (brewingStand.getStack(TileEntityBrewingStandCustom.SLOT_MODIFIER).isNotEmpty){
			drawTexturedModalRect(x + 62, y + 45, 197, 0, 14, 2);
		}
	}
}
