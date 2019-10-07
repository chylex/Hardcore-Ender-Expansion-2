package chylex.hee.game.mechanics.trinket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

/**
 * Describes an item which can be inserted into a Trinket slot. The interface must be applied to a class extending [Item][net.minecraft.item.Item].
 */
interface ITrinketItem{
	/**
	 * Returns true if the Trinket can be activated, i.e. it can be inserted into a Trinket slot and then recognized by [ITrinketHandler.isItemActive] and [ITrinketHandler.transformIfActive].
	 */
	@JvmDefault
	fun canPlaceIntoTrinketSlot(stack: ItemStack) = true
	
	/**
	 * Spawns particle and/or sound effects on the [target] entity whose trinket broke.
	 */
	@Sided(Side.CLIENT)
	@JvmDefault
	fun spawnClientTrinketBreakFX(target: Entity){}
}
