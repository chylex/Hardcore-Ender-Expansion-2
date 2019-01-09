package chylex.hee.game.item
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.init.ModItems
import chylex.hee.system.util.over
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumHand
import net.minecraft.world.World

class ItemSpatialDashGem : ItemAbstractEnergyUser(){
	companion object{
		fun setupRecipeNBT(recipe: IRecipe){
			ModItems.SPATIAL_DASH_GEM.setEnergyChargePercentage(recipe.recipeOutput, 1F)
		}
	}
	
	// Energy properties
	
	override fun getEnergyCapacity(stack: ItemStack) =
		Units(90)
	
	override fun getEnergyPerUse(stack: ItemStack) =
		3 over 2
	
	// Item handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		
		if (!useEnergyUnit(stack)){
			return ActionResult(FAIL, stack)
		}
		
		if (!world.isRemote){
			world.spawnEntity(EntityProjectileSpatialDash(world, player))
		}
		
		player.cooldownTracker.setCooldown(this, 24)
		player.addStat(StatList.getObjectUseStats(this)!!)
		
		return ActionResult(SUCCESS, stack)
	}
}
