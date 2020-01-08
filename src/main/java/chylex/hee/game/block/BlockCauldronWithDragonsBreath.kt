package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.item.ItemStack

class BlockCauldronWithDragonsBreath(builder: BlockBuilder) : BlockAbstractCauldron(builder){
	override fun createFilledBucket(): ItemStack?{
		return null
	}
	
	override fun createFilledBottle(): ItemStack?{
		return ItemStack(Items.DRAGON_BREATH)
	}
}
