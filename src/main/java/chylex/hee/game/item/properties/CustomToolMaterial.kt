package chylex.hee.game.item.properties
import chylex.hee.game.item.Tool.Level.DIAMOND
import chylex.hee.game.item.Tool.Level.IRON
import chylex.hee.game.item.Tool.Level.WOOD
import chylex.hee.init.ModItems
import chylex.hee.system.MagicValues
import net.minecraft.item.IItemTier
import net.minecraft.item.crafting.Ingredient

object CustomToolMaterial{
	val VOID_MINER: IItemTier      = Tier(IRON,    925, 15F, 0F, 1, Ingredient.fromItems(ModItems.VOID_ESSENCE))
	val VOID_BUCKET: IItemTier     = Tier(WOOD,    575, 15F, 0F, 1, Ingredient.fromItems(ModItems.VOID_ESSENCE))
	val SCORCHING_TOOL: IItemTier  = Tier(DIAMOND, 175, 10F, 2F, 0, Ingredient.fromItems(ModItems.INFERNIUM_INGOT))
	val SCORCHING_SWORD: IItemTier = Tier(DIAMOND, 275, 10F, 9F - MagicValues.PLAYER_HAND_DAMAGE, 0, Ingredient.fromItems(ModItems.INFERNIUM_INGOT))
	
	private class Tier(
		private val harvestLevel: Int,
		private val maxUses: Int,
		private val efficiency: Float,
		private val attackDamage: Float,
		private val enchantability: Int,
		private val repairMaterial: Ingredient
	) : IItemTier{
		override fun getHarvestLevel() = harvestLevel
		override fun getMaxUses() = maxUses
		override fun getEfficiency() = efficiency
		override fun getAttackDamage() = attackDamage
		override fun getEnchantability() = enchantability
		override fun getRepairMaterial() = repairMaterial
	}
}
