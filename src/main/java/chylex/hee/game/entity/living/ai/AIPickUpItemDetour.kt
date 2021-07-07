package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.util.math.square
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.Goal.Flag.JUMP
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import net.minecraft.entity.item.ItemEntity
import net.minecraft.item.ItemStack
import java.util.EnumSet

class AIPickUpItemDetour(
	private val entity: MobEntity,
	private val chancePerTick: Int,
	private val maxDetourTicks: IntRange,
	private val searchRadius: Double,
	private val speedMp: Double = 1.0,
	private val handler: IItemPickupHandler,
) : Goal() {
	interface IItemPickupHandler {
		fun canPickUp(stack: ItemStack): Boolean
		fun onPickUp(stack: ItemStack)
	}
	
	private var targetItem: ItemEntity? = null
	private var remainingTicks = 0
	
	init {
		mutexFlags = EnumSet.of(MOVE, JUMP)
	}
	
	override fun shouldExecute(): Boolean {
		val rand = entity.rng
		
		if (rand.nextInt(chancePerTick) != 0) {
			return false
		}
		
		val nearbyItems = entity.world.selectExistingEntities.inRange<ItemEntity>(entity.posVec, searchRadius).filter { entity.canEntityBeSeen(it) && handler.canPickUp(it.item) }
		val selectedItem = rand.nextItemOrNull(nearbyItems)
		
		if (selectedItem == null) {
			return false
		}
		
		targetItem = selectedItem
		remainingTicks = rand.nextInt(maxDetourTicks)
		return true
	}
	
	override fun shouldContinueExecuting(): Boolean {
		return targetItem?.let { it.isAlive && it.getDistanceSq(entity) <= square(searchRadius) } == true && --remainingTicks >= 0
	}
	
	override fun tick() {
		val targetItem = targetItem!!
		
		if (!entity.navigator.tryMoveToXYZ(targetItem.posX, targetItem.posY, targetItem.posZ, speedMp)) {
			entity.moveHelper.setMoveTo(targetItem.posX, targetItem.posY, targetItem.posZ, speedMp)
		}
		
		if (entity.getDistance(targetItem) < 0.5 * (entity.width + targetItem.width)) {
			val targetStack = targetItem.item
			
			handler.onPickUp(targetStack.split(1))
			
			if (targetStack.isEmpty) {
				targetItem.remove()
			}
			else {
				targetItem.item = targetStack
			}
			
			remainingTicks = 0
		}
	}
	
	override fun resetTask() {
		targetItem = null
	}
}
