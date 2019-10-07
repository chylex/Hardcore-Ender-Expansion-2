package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModGuiHandler.GuiType.LOOT_CHEST
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class BlockLootChest(builder: BlockBuilder) : BlockAbstractChest<TileEntityLootChest>(builder){
	override val guiType = LOOT_CHEST
	override fun createNewTileEntity() = TileEntityLootChest()
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		lines.add(I18n.format("tile.hee.loot_chest.tooltip"))
	}
}
