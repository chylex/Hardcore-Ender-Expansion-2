package chylex.hee.game.container.slot
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.SlotItemHandler

open class SlotTrinketItem(trinketHandler: IItemHandler, slotIndex: Int, x: Int, y: Int) : SlotItemHandler(trinketHandler, slotIndex, x, y){
	@Sided(Side.CLIENT)
	protected object Client{
		val TEX_SLOT = Resource.Custom("textures/gui/trinket.png")
		
		const val TEX_SLOT_W = 64F
		const val TEX_SLOT_H = 32F
		
		val TEX_DEFINITION = object : TextureAtlasSprite(TEX_SLOT.toString()){
			override fun getMinU() = 19F / TEX_SLOT_W
			override fun getMaxU() = 35F / TEX_SLOT_W
			override fun getMinV() =  1F / TEX_SLOT_H
			override fun getMaxV() = 17F / TEX_SLOT_H
		}
	}
	
	override fun isItemValid(stack: ItemStack) = (stack.item as? ITrinketItem)?.canPlaceIntoTrinketSlot(stack) == true
	override fun getSlotStackLimit() = 1
	
	@Sided(Side.CLIENT) override fun getBackgroundLocation() = Client.TEX_SLOT
	@Sided(Side.CLIENT) override fun getBackgroundSprite() = Client.TEX_DEFINITION
}
