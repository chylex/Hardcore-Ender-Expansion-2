package chylex.hee.game.item
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects.WEAKNESS
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.Random

class ItemChorusBerry : ItemFood(0, 0F, false){
	private companion object{
		private const val MIN_TELEPORT_DISTANCE = 8
		private const val MIN_TELEPORT_DISTANCE_SQ = MIN_TELEPORT_DISTANCE * MIN_TELEPORT_DISTANCE
		
		private val DAMAGE_TELEPORT_FAIL = Damage(PEACEFUL_EXCLUSION, MAGIC_TYPE)
		
		private fun teleportPlayer(player: EntityPlayer, strength: Int, rand: Random): Boolean{
			val world = player.world
			val playerPos = player.position
			
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
					val finalPos = targetPos.offsetUntil(DOWN, 1..teleportYSearchRange){ it.blocksMovement(world) }?.up() ?: targetPos.down(teleportYSearchRange)
					
					if (finalPos.distanceSqTo(player) > MIN_TELEPORT_DISTANCE_SQ){
						val event = EnderTeleportEvent(player, finalPos.x + 0.5, finalPos.y + 0.01, finalPos.z + 0.5, 0F)
						
						if (MinecraftForge.EVENT_BUS.post(event)){
							return false
						}
						
						if (player.isRiding){
							player.dismountRidingEntity()
						}
						
						player.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ) // TODO fx
						player.fallDistance = 0F
						return true
					}
				}
				
				teleportYMax += 2
			}
			
			return false
		}
	}
	
	init{
		unlocalizedName = "hee.chorus_berry"
		setAlwaysEdible()
	}
	
	override fun onFoodEaten(stack: ItemStack, world: World, player: EntityPlayer){
		if (!world.isRemote){
			val rand = world.rand
			val foodStats = player.foodStats
			
			val hungerRestored = rand.nextInt(1, 3)
			val hungerOvercharge = (foodStats.foodLevel + hungerRestored - 20).coerceAtLeast(0)
			
			if (hungerOvercharge == 0){
				foodStats.addStats(hungerRestored, 3.5F)
				
				if (rand.nextInt(4) == 0){
					player.addPotionEffect(PotionEffect(WEAKNESS, 20 * 60, 1))
				}
			}
			else{
				foodStats.addStats(hungerRestored, 16.5F)
				
				if (rand.nextInt(4) != 0){
					player.addPotionEffect(PotionEffect(WEAKNESS, 20 * (90 + hungerOvercharge * 30), 1))
				}
				
				if (!teleportPlayer(player, hungerOvercharge, rand)){
					DAMAGE_TELEPORT_FAIL.dealTo(1F, player, Damage.TITLE_MAGIC)
				}
			}
		}
	}
}
