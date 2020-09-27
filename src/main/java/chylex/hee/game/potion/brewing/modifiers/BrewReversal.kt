package chylex.hee.game.potion.brewing.modifiers
import chylex.hee.game.potion.brewing.IBrewingModifier
import chylex.hee.game.potion.brewing.PotionBrewing
import chylex.hee.system.migration.Items
import net.minecraft.item.ItemStack

object BrewReversal : IBrewingModifier{
	override val ingredient = Items.FERMENTED_SPIDER_EYE!!
	
	override fun check(input: ItemStack): Boolean{
		return PotionBrewing.unpack(input)?.canReverse == true
	}
	
	override fun apply(input: ItemStack): ItemStack{
		return PotionBrewing.unpack(input)!!.afterReversal ?: input
	}
}
