package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseWanderConditioned
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.math.Vec3d

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
	
	override fun getPosition(): Vec3d?{
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
					return testPos.offsetUntil(UP, 1..256){ it.getMaterial(world) !== Material.WATER }?.down()?.center
				}
			}
		}
		
		if (searchLandChance.let { it == null || rand.nextFloat() < it() }){
			return RandomPositionGenerator.findRandomTarget(entity, maxLandDistanceXZ, maxLandDistanceY)
		}
		
		return null
	}
}
