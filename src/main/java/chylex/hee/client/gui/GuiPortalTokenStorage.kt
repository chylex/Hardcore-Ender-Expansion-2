package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.any
import chylex.hee.system.util.nonEmptySlots
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
class GuiPortalTokenStorage(player: EntityPlayer, tile: TileEntityVoidPortalStorage) : GuiBaseChestContainer(ContainerPortalTokenStorage(player, tile)){
	fun canActivateToken(stack: ItemStack): Boolean{
		return (inventorySlots as ContainerPortalTokenStorage).lowerChestInventory.nonEmptySlots.any { it.stack === stack }
	}
}
