package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModGuiHandler.GuiType.LOOT_CHEST
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IBlockReader

class BlockLootChest(builder: BlockBuilder) : BlockAbstractChest<TileEntityLootChest>(builder){
	override val guiType = LOOT_CHEST
	override fun createTileEntity() = TileEntityLootChest()
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TextComponentTranslation("block.hee.loot_chest.tooltip"))
	}
}
