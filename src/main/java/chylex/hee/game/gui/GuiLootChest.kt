package chylex.hee.game.gui
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.gui.base.GuiBaseChestContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiLootChest(player: EntityPlayer, tile: TileEntityLootChest) : GuiBaseChestContainer(ContainerLootChest(player, tile))
