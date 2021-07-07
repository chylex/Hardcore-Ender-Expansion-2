package chylex.hee.client.gui.screen

import chylex.hee.game.container.ContainerShulkerBox
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiShulkerBox(container: ContainerShulkerBox, inventory: PlayerInventory, title: ITextComponent) : AbstractChestContainerScreen<ContainerShulkerBox>(container, inventory, title)
