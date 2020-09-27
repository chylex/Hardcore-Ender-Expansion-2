package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiLootChest(container: ContainerLootChest, inventory: PlayerInventory, title: ITextComponent) : GuiBaseChestContainer<ContainerLootChest>(container, inventory, TileEntityLootChest.getClientTitle(inventory.player, title))
