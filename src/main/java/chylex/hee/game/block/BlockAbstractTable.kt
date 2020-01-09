package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.storage.loot.LootContext

abstract class BlockAbstractTable(builder: BlockBuilder, val tier: Int, val firstTier: Int) : BlockSimple(builder){
	init{
		require(tier in 1..3){ "[BlockAbstractTable] tier must be in the range 1..3" }
		require(firstTier <= tier){ "[BlockAbstractTable] firstTier cannot be larger than current tier" }
	}
	
	override fun isSolid(state: BlockState): Boolean{
		return true
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		return super.getDrops(state, context) // UPDATE
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TextComponentTranslation("block.tooltip.hee.table.tier", tier))
	}
	
	override fun getRenderLayer() = CUTOUT
}
