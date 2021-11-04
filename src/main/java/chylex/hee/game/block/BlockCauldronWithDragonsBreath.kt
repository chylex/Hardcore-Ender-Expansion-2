package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class BlockCauldronWithDragonsBreath(builder: BlockBuilder) : BlockAbstractCauldron(builder) {
	override val model
		get() = BlockStateModels.Cauldron(Resource.Custom("block/dragons_breath_still"))
	
	override fun createFilledBucket(): ItemStack? {
		return null
	}
	
	override fun createFilledBottle(): ItemStack {
		return ItemStack(Items.DRAGON_BREATH)
	}
}
