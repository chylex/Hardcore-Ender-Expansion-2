package chylex.hee.game.mechanics.instability.dimension.components

import chylex.hee.game.potion.makeEffect
import chylex.hee.game.world.Pos
import chylex.hee.game.world.blocksMovement
import chylex.hee.game.world.center
import chylex.hee.game.world.isTopSolid
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.spawn
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextVector
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

abstract class EndermiteSpawnLogic {
	private val rand = Random()
	
	protected abstract fun checkMobLimits(world: ServerWorld, pos: BlockPos): Boolean
	
	abstract fun countExisting(world: ServerWorld, pos: BlockPos): Int
	
	fun trySpawnNear(world: World, pos: BlockPos): Boolean {
		if (world !is ServerWorld || !checkMobLimits(world, pos)) {
			return false
		}
		
		repeat(20) {
			val randomPos = Pos(pos.center.add(rand.nextVector(rand.nextFloat(8.0, 64.0))))
			val finalPos = randomPos.offsetUntil(UP, -8..8) { !it.blocksMovement(world) && it.down().isTopSolid(world) }
			
			if (finalPos != null) {
				world.spawn(ModEntities.ENDERMITE_INSTABILITY, finalPos.x + 0.5, finalPos.y + 0.01, finalPos.z + 0.5, yaw = rand.nextFloat(0F, 360F)) {
					addPotionEffect(Potions.RESISTANCE.makeEffect(20, 5, isAmbient = false, showParticles = false))
				}
				
				// TODO particles and spawn sound
				return true
			}
		}
		
		return false
	}
}
