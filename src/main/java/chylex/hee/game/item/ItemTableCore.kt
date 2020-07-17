package chylex.hee.game.item
import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.BlockTableBase
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.setBlock
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemTableCore(private val tableBlocks: Array<BlockAbstractTableTile<*>>, properties: Properties) : Item(properties){
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		val block = pos.getBlock(world)
		
		if (block is BlockTableBase){
			val table = tableBlocks.find { it.tier == block.tier } ?: return FAIL
			
			if (!world.isRemote){
				pos.breakBlock(world, false)
				pos.setBlock(world, table)
			}
			
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TranslationTextComponent("item.tooltip.hee.table_core.tooltip", tableBlocks.map { it.tier }.min()))
	}
}
