package chylex.hee.client.gui

import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.container.ContainerShulkerBox
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiShulkerBox(container: ContainerShulkerBox, inventory: PlayerInventory, title: ITextComponent) : GuiBaseChestContainer<ContainerShulkerBox>(container, inventory, title)
