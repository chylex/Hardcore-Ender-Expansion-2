package chylex.hee.game.item

import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.item.ItemAbstractEnergyUser.EnergyItem
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IUseItemOnAirComponent
import chylex.hee.game.item.infusion.Infusion.CAPACITY
import chylex.hee.game.item.infusion.Infusion.DISTANCE
import chylex.hee.game.item.infusion.Infusion.SPEED
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.over
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.world.World
import org.apache.commons.lang3.math.Fraction

object ItemSpatialDashGem : HeeItemBuilder() {
	private const val INSTANT_SPEED_MP = 100F // just above the maximum possible distance
	
	private val ENERGY = object : EnergyItem() {
		override fun getEnergyCapacity(stack: ItemStack): Units {
			return Units((90 * InfusionTag.getList(stack).calculateLevelMultiplier(CAPACITY, 1.75F)).floorToInt())
		}
		
		override fun getEnergyPerUse(stack: ItemStack): Fraction {
			return 3 over 2
		}
	}
	
	init {
		includeFrom(ItemAbstractInfusable())
		includeFrom(ItemAbstractEnergyUser(ENERGY))
		
		maxStackSize = 1
		
		components.useOnAir = object : IUseItemOnAirComponent {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				if (!ENERGY.useUnit(player, heldItem)) {
					return ActionResult(FAIL, heldItem)
				}
				
				if (!world.isRemote) {
					val infusions = InfusionTag.getList(heldItem)
					
					val speedMp = when (infusions.determineLevel(SPEED)) {
						2    -> INSTANT_SPEED_MP
						1    -> 1.75F
						else -> 1F
					}
					
					val distanceMp = infusions.calculateLevelMultiplier(DISTANCE, 1.75F)
					
					world.addEntity(EntityProjectileSpatialDash(world, player, speedMp, distanceMp))
				}
				
				player.cooldownTracker.setCooldown(heldItem.item, 24)
				player.addStat(Stats.ITEM_USED[heldItem.item])
				
				return ActionResult(SUCCESS, heldItem)
			}
		}
	}
}
