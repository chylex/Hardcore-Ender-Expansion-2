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
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiBrewingStandCustom(inventory: InventoryPlayer, private val brewingStand: TileEntityBrewingStandCustom) : GuiBrewingStand(inventory, brewingStand){
	private companion object{
		private val TEX_BACKGROUND = Resource.Custom("textures/gui/brewing_stand.png")
		private val BUBBLE_LENGTHS = intArrayOf(0, 6, 11, 16, 20, 24, 29)
	}
	
	private var brewStartTime = brewingStand.world.totalWorldTime
	
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
		
		val worldTime = brewingStand.world.totalWorldTime
		val brewTime = brewingStand.brewTime
		
		if (brewTime > 0){
			val brewProgress = (28F * (1F - (brewTime / 400F))).toInt()
			
			if (brewProgress > 0){
				drawTexturedModalRect(x + 97, y + 16, 176, 0, 9, brewProgress)
			}
			
			val bubbleLength = BUBBLE_LENGTHS[((worldTime - brewStartTime).toInt() / 2) % 7]
			
			if (bubbleLength > 0){
				drawTexturedModalRect(x + 63, y + 43 - bubbleLength, 185, 29 - bubbleLength, 12, bubbleLength)
			}
		}
		else{
			brewStartTime = worldTime
		}
		
		if (brewingStand.getStack(TileEntityBrewingStandCustom.SLOT_MODIFIER).isNotEmpty){
			drawTexturedModalRect(x + 62, y + 45, 197, 0, 14, 2);
		}
	}
}
