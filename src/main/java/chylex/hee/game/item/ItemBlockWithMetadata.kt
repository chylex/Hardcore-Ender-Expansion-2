package chylex.hee.game.item
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock

class ItemBlockWithMetadata(sourceBlock: Block) : ItemBlock(sourceBlock){
	init{
		hasSubtypes = true
	}
	
	override fun getMetadata(damage: Int) = damage
}
