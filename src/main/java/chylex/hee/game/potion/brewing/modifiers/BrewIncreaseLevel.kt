package chylex.hee.game.potion.brewing.modifiers
import chylex.hee.game.potion.brewing.IBrewingModifier
import chylex.hee.game.potion.brewing.PotionBrewing
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object BrewIncreaseLevel : IBrewingModifier{
	override val ingredient = Items.GLOWSTONE_DUST!!
	
	override fun check(input: ItemStack): Boolean{
		return PotionBrewing.unpack(input)?.canIncreaseLevel == true
	}
	
	override fun apply(input: ItemStack): ItemStack{
		return PotionBrewing.unpack(input)!!.withIncreasedLevel
	}
}
