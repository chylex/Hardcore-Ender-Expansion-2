package chylex.hee.game.mechanics.instability.dimension.components

import chylex.hee.game.potion.util.makeInstance
import chylex.hee.game.world.util.blocksMovement
import chylex.hee.game.world.util.isTopSolid
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.util.math.Pos
import chylex.hee.util.math.center
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextVector
import net.minecraft.potion.Effects
import net.minecraft.util.Direction.UP
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
					addPotionEffect(Effects.RESISTANCE.makeInstance(20, 5, isAmbient = false, showParticles = false))
				}
				
				// TODO particles and spawn sound
				return true
			}
		}
		
		return false
	}
}
