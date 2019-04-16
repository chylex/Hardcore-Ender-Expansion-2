package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.container.ContainerPortalTokenStorage
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiPortalTokenStorage(player: EntityPlayer, tile: TileEntityVoidPortalStorage) : GuiBaseChestContainer(ContainerPortalTokenStorage(player, tile))
