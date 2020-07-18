package chylex.hee.game.item
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.world.util.Teleporter
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityPlayerMP
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntilExcept
import chylex.hee.system.util.playUniversal
import chylex.hee.system.util.posVec
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.item.Food
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import java.util.Random

class ItemChorusBerry(properties: Properties) : Item(properties){
	companion object{
		private const val MIN_TELEPORT_DISTANCE = 8
		private const val MIN_TELEPORT_DISTANCE_SQ = MIN_TELEPORT_DISTANCE * MIN_TELEPORT_DISTANCE
		
		private val TELEPORT = Teleporter(causedInstability = 10u)
		
		private val DAMAGE_TELEPORT_FAIL = Damage(PEACEFUL_EXCLUSION, MAGIC_TYPE)
		
		val FOOD: Food = Food.Builder().hunger(0).saturation(0F).setAlwaysEdible().build()
		
		private fun teleportEntity(entity: EntityLivingBase, strength: Int, rand: Random): Boolean{
			val world = entity.world
			val playerPos = entity.position
			
			val teleportDistance = strength * 24
			val teleportYSearchRange = strength * 12
			
			val teleportYMin = -teleportDistance
			var teleportYMax = -teleportDistance / 4
			
			while(teleportYMax <= teleportDistance){
				val targetPos = playerPos.add(
					rand.nextInt(-teleportDistance, teleportDistance),
					rand.nextInt(teleportYMin, teleportYMax),
					rand.nextInt(-teleportDistance, teleportDistance)
				)
				
				if (targetPos.y > 0 && !targetPos.blocksMovement(world) && !targetPos.up().blocksMovement(world)){
					val finalPos = targetPos.offsetUntilExcept(DOWN, 1..teleportYSearchRange){ it.blocksMovement(world) } ?: targetPos.down(teleportYSearchRange)
					
					if (finalPos.distanceSqTo(entity) > MIN_TELEPORT_DISTANCE_SQ){
						return TELEPORT.toBlock(entity, finalPos)
					}
				}
				
				teleportYMax += 2
			}
			
			return false
		}
	}
	
	override fun getTranslationKey(): String{
		return "item.hee.chorus_berry"
	}
	
	override fun onItemUseFinish(stack: ItemStack, world: World, entity: EntityLivingBase): ItemStack{
		val rand = world.rand
		
		if (entity is EntityPlayer){
			entity.addStat(Stats.useItem(this))
			entity.getEatSound(stack).playUniversal(entity, entity.posVec, SoundCategory.NEUTRAL, volume = 1F, pitch = rand.nextFloat(0.6F, 1.4F))
			SoundEvents.ENTITY_PLAYER_BURP.playUniversal(entity, entity.posVec, SoundCategory.PLAYERS, volume = 0.5F, pitch = rand.nextFloat(0.9F, 1.0F))
		}
		
		if (!world.isRemote){
			if (entity is EntityPlayerMP){
				CriteriaTriggers.CONSUME_ITEM.trigger(entity, stack)
			}
			
			val foodStats = (entity as? EntityPlayer)?.foodStats
			
			val hungerRestored = rand.nextInt(1, 3)
			val hungerOvercharge = if (foodStats == null) hungerRestored else (foodStats.foodLevel + hungerRestored - 20).coerceAtLeast(0)
			
			if (hungerOvercharge == 0){
				foodStats?.addStats(hungerRestored, 3.5F)
				
				if (rand.nextInt(4) == 0){
					entity.addPotionEffect(Potions.WEAKNESS.makeEffect(20 * 60, 1))
				}
			}
			else{
				foodStats?.addStats(hungerRestored, 16.5F)
				
				if (rand.nextInt(4) != 0){
					entity.addPotionEffect(Potions.WEAKNESS.makeEffect(20 * (90 + hungerOvercharge * 30), 1))
				}
				
				if (!teleportEntity(entity, hungerOvercharge, rand)){
					DAMAGE_TELEPORT_FAIL.dealTo(1F, entity, Damage.TITLE_MAGIC)
				}
			}
		}
		
		return stack.apply { shrink(1) }
	}
}
