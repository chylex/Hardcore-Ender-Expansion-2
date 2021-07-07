package chylex.hee.client.gui.screen

import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiPortalTokenStorage(container: ContainerPortalTokenStorage, inventory: PlayerInventory, title: ITextComponent) : AbstractChestContainerScreen<ContainerPortalTokenStorage>(container, inventory, title) {
	fun canActivateToken(stack: ItemStack): Boolean {
		return container.canActivateToken(stack)
	}
}
