package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.init.ModGuiHandler.GuiType.LOOT_CHEST
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class BlockLootChest(builder: BlockSimple.Builder) : BlockAbstractChest<TileEntityLootChest>(builder){
	override val guiType = LOOT_CHEST
	override fun createNewTileEntity() = TileEntityLootChest()
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		lines.add(I18n.format("tile.hee.loot_chest.tooltip"))
	}
}
