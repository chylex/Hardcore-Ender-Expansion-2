package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.any
import chylex.hee.system.util.nonEmptySlots
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiPortalTokenStorage(container: ContainerPortalTokenStorage, inventory: PlayerInventory, title: ITextComponent) : GuiBaseChestContainer<ContainerPortalTokenStorage>(container, inventory, title){
	fun canActivateToken(stack: ItemStack): Boolean{
		return container.lowerChestInventory.nonEmptySlots.any { it.stack === stack }
	}
}
