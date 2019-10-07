package chylex.hee.game.block
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.item.ItemStack

class BlockCauldronWithDragonsBreath : BlockAbstractCauldron(){
	override fun createFilledBucket(): ItemStack?{
		return null
	}
	
	override fun createFilledBottle(): ItemStack?{
		return ItemStack(Items.DRAGON_BREATH)
	}
}
