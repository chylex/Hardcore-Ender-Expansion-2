package chylex.hee.system.util.facades
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.stats.StatList

object Stats{
	fun harvestBlock(block: Block) = StatList.getBlockStats(block)!!
	fun craftItem(item: Item) = StatList.getCraftStats(item)!!
	fun useItem(item: Item) = StatList.getObjectUseStats(item)!!
	fun breakItem(item: Item) = StatList.getObjectBreakStats(item)!!
	
	val CAULDRON_FILLED get() = StatList.CAULDRON_FILLED!!
	val CAULDRON_USED get() = StatList.CAULDRON_USED!!
	val FLOWER_POTTED get() = StatList.FLOWER_POTTED!!
}
