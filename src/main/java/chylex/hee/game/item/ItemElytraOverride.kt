package chylex.hee.game.item
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.cleanupNBT
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Enchantments
import net.minecraft.inventory.EntityEquipmentSlot.CHEST
import net.minecraft.item.ItemElytra
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import kotlin.math.min
import kotlin.math.sqrt

class ItemElytraOverride : ItemElytra(){
	private companion object{
		private const val COUNTER_TAG = "Counter"
		private const val LAST_POS_TAG = "LastPos"
		private const val X_TAG = "X"
		private const val Y_TAG = "Y"
		private const val Z_TAG = "Z"
		
		private fun createPositionTag(entity: Entity) = TagCompound().also {
			it.setDouble(X_TAG, entity.posX)
			it.setDouble(Y_TAG, entity.posY)
			it.setDouble(Z_TAG, entity.posZ)
		}
		
		private fun calculateCounterIncrement(entity: Entity, lastPosTag: TagCompound): Float{
			val distance = entity.getDistance(lastPosTag.getDouble(X_TAG), lastPosTag.getDouble(Y_TAG), lastPosTag.getDouble(Z_TAG))
			return sqrt(min(distance.toFloat(), 60F)) / 7.25F
		}
		
		private fun removeFlightTrackingTags(stack: ItemStack){
			with(stack.heeTagOrNull ?: return){
				if (hasKey(COUNTER_TAG)){
					removeTag(COUNTER_TAG)
					removeTag(LAST_POS_TAG)
					stack.cleanupNBT()
				}
			}
		}
	}
	
	init{
		translationKey = "elytra"
	}
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || entity !is EntityPlayerMP){
			return
		}
		
		if (entity.isElytraFlying && stack === entity.getItemStackFromSlot(CHEST) && !entity.capabilities.isCreativeMode){
			with(stack.heeTag){
				if (!hasKey(LAST_POS_TAG)){
					setFloat(COUNTER_TAG, 0F)
					setTag(LAST_POS_TAG, createPositionTag(entity))
				}
				else if (world.totalWorldTime % 20L == 0L){
					var newCounter = getFloat(COUNTER_TAG) + calculateCounterIncrement(entity, getCompoundTag(LAST_POS_TAG))
					val damageAfter = 1F + (0.33F * EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack))
					
					while(newCounter >= damageAfter && isUsable(stack)){
						newCounter -= damageAfter
						
						val newDamage = stack.itemDamage + 1
						
						CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(entity, stack, newDamage)
						super.setDamage(stack, newDamage)
					}
					
					setFloat(COUNTER_TAG, newCounter)
					setTag(LAST_POS_TAG, createPositionTag(entity))
				}
			}
		}
		else{
			removeFlightTrackingTags(stack)
		}
	}
	
	override fun setDamage(stack: ItemStack, damage: Int){
		if (!stack.heeTagOrNull.hasKey(COUNTER_TAG)){ // prevent vanilla from damaging the item while flying
			super.setDamage(stack, damage)
		}
	}
}
