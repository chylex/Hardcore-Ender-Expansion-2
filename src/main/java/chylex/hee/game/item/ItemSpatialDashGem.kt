package chylex.hee.game.item
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.Infusion.CAPACITY
import chylex.hee.game.item.infusion.Infusion.DISTANCE
import chylex.hee.game.item.infusion.Infusion.SPEED
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.init.ModItems
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.over
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World

class ItemSpatialDashGem : ItemAbstractEnergyUser(), IInfusableItem{
	companion object{
		private const val INSTANT_SPEED_MP = 100F // just above the maximum possible distance
		
		fun setupRecipeNBT(stack: ItemStack){
			ModItems.SPATIAL_DASH_GEM.setEnergyChargePercentage(stack, 1F)
		}
	}
	
	// Energy properties
	
	override fun getEnergyCapacity(stack: ItemStack) =
		Units((90 * InfusionTag.getList(stack).calculateLevelMultiplier(CAPACITY, 1.75F)).floorToInt())
	
	override fun getEnergyPerUse(stack: ItemStack) =
		3 over 2
	
	// Item handling
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (!useEnergyUnit(heldItem)){
			return ActionResult(FAIL, heldItem)
		}
		
		if (!world.isRemote){
			val infusions = InfusionTag.getList(heldItem)
			
			val speedMp = when(infusions.determineLevel(SPEED)){
				2 -> INSTANT_SPEED_MP
				1 -> 1.75F
				else -> 1F
			}
			
			val distanceMp = infusions.calculateLevelMultiplier(DISTANCE, 1.75F)
			
			world.spawnEntity(EntityProjectileSpatialDash(world, player, speedMp, distanceMp))
		}
		
		player.cooldownTracker.setCooldown(this, 24)
		player.addStat(Stats.useItem(this))
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
}
