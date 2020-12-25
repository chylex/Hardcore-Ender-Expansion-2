package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class BlockCauldronWithDragonsBreath(builder: BlockBuilder) : BlockAbstractCauldron(builder) {
	override fun createFilledBucket(): ItemStack? {
		return null
	}
	
	override fun createFilledBottle(): ItemStack? {
		return ItemStack(Items.DRAGON_BREATH)
	}
}
