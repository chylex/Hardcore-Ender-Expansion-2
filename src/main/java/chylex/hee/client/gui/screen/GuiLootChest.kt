package chylex.hee.client.gui.screen

import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiLootChest(container: ContainerLootChest, inventory: PlayerInventory, title: ITextComponent) : AbstractChestContainerScreen<ContainerLootChest>(container, inventory, TileEntityLootChest.getClientTitle(inventory.player, title))
