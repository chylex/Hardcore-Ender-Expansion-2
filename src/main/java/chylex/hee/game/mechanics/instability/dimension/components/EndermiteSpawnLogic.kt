package chylex.hee.game.mechanics.instability.dimension.components
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.Pos
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.getFaceShape
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetUntil
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

abstract class EndermiteSpawnLogic{
	private val rand = Random()
	
	protected abstract fun checkMobLimits(world: World, pos: BlockPos): Boolean
	
	abstract fun countExisting(world: World, pos: BlockPos): Int
	
	fun trySpawnNear(world: World, pos: BlockPos): Boolean{
		if (!checkMobLimits(world, pos)){
			return false
		}
		
		repeat(20){
			val randomPos = Pos(pos.center.add(rand.nextVector(rand.nextFloat(8.0, 64.0))))
			val finalPos = randomPos.offsetUntil(UP, -8..8){ !it.blocksMovement(world) && it.down().getFaceShape(world, UP) == SOLID }
			
			if (finalPos != null){
				EntityMobEndermiteInstability(world).apply {
					setLocationAndAngles(finalPos.x + 0.5, finalPos.y + 0.01, finalPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
					addPotionEffect(Potions.RESISTANCE.makeEffect(20, 5, isAmbient = false, showParticles = false))
					world.spawnEntity(this)
				}
				
				// TODO particles and spawn sound
				return true
			}
		}
		
		return false
	}
}
