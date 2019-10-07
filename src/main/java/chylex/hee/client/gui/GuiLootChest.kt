package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.entity.player.EntityPlayer

@Sided(Side.CLIENT)
class GuiLootChest(player: EntityPlayer, tile: TileEntityLootChest) : GuiBaseChestContainer(ContainerLootChest(player, tile))
