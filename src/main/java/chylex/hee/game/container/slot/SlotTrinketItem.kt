package chylex.hee.game.container.slot
import chylex.hee.game.item.trinket.ITrinketItem
import chylex.hee.system.Resource
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.SlotItemHandler

open class SlotTrinketItem(trinketHandler: IItemHandler, slotIndex: Int, x: Int, y: Int) : SlotItemHandler(trinketHandler, slotIndex, x, y){
	protected companion object{
		@JvmStatic
		@SideOnly(Side.CLIENT)
		protected val TEX_SLOT = Resource.Custom("textures/gui/trinket.png")
		
		const val TEX_SLOT_W = 64F
		const val TEX_SLOT_H = 32F
		
		@JvmStatic
		@SideOnly(Side.CLIENT)
		private val TEX_DEFINITION = object : TextureAtlasSprite(TEX_SLOT.toString()){
			override fun getMinU(): Float = 19F / TEX_SLOT_W
			override fun getMaxU(): Float = 35F / TEX_SLOT_W
			override fun getMinV(): Float =  1F / TEX_SLOT_H
			override fun getMaxV(): Float = 17F / TEX_SLOT_H
		}
	}
	
	override fun isItemValid(stack: ItemStack): Boolean = (stack.item as? ITrinketItem)?.canPlaceIntoTrinketSlot(stack) == true
	override fun getSlotStackLimit(): Int = 1
	
	@SideOnly(Side.CLIENT) override fun getBackgroundLocation(): ResourceLocation = TEX_SLOT
	@SideOnly(Side.CLIENT) override fun getBackgroundSprite(): TextureAtlasSprite = TEX_DEFINITION
}
