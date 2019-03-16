package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.ContainerLootChest
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiLootChest(player: EntityPlayer, tile: TileEntityLootChest) : GuiBaseChestContainer(ContainerLootChest(player, tile))
