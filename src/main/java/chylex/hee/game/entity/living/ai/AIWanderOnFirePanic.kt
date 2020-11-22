package chylex.hee.game.entity.living.ai
import chylex.hee.game.world.Pos
import chylex.hee.game.world.center
import chylex.hee.game.world.getMaterial
import chylex.hee.game.world.offsetUntilExcept
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.random.nextInt
import net.minecraft.block.material.Material
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.util.math.vector.Vector3d

class AIWanderOnFirePanic(
	entity: EntityCreature,
	movementSpeed: Double,
	private val searchWaterChance: (() -> Float)? = null,
	private val maxWaterDistanceXZ: Int,
	private val maxWaterDistanceY: Int,
	private val searchLandChance: (() -> Float)? = null,
	private val maxLandDistanceXZ: Int,
	private val maxLandDistanceY: Int
) : AIBaseWanderConditioned(entity, movementSpeed){
	override fun shouldExecute(): Boolean{
		if (!entity.isBurning){
			return false
		}
		
		val newTarget = position ?: return false
		
		setTarget(newTarget)
		return true
	}
	
	override fun shouldContinueExecuting(): Boolean{
		return entity.isBurning && !entity.navigator.noPath()
	}
	
	override fun getPosition(): Vector3d?{
		val world = entity.world
		val rand = entity.rng
		
		if (searchWaterChance.let { it == null || rand.nextFloat() < it() }){
			val start = Pos(entity)
			
			for(attempt in 1..100){
				val testPos = start.add(
					rand.nextInt(-maxWaterDistanceXZ, maxWaterDistanceXZ),
					rand.nextInt(-maxWaterDistanceY, maxWaterDistanceY),
					rand.nextInt(-maxWaterDistanceXZ, maxWaterDistanceXZ)
				)
				
				if (testPos.getMaterial(world) === Material.WATER){
					return testPos.offsetUntilExcept(UP, 1..256){ it.getMaterial(world) !== Material.WATER }?.center
				}
			}
		}
		
		if (searchLandChance.let { it == null || rand.nextFloat() < it() }){
			return RandomPositionGenerator.findRandomTarget(entity, maxLandDistanceXZ, maxLandDistanceY)
		}
		
		return null
	}
}
