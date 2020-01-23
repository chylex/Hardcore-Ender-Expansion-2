package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseWanderConditioned
import chylex.hee.system.migration.vanilla.EntityCreature
import chylex.hee.system.util.Pos
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.isAir
import chylex.hee.system.util.nextInt
import net.minecraft.pathfinding.Path
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY

class AIWanderLightStartle(
	entity: EntityCreature,
	movementSpeed: Double,
	private val minBlockLightIncrease: Int,
	private val minCombinedLightDecrease: Int,
	private val searchAttempts: Int,
	private val maxDistanceXZ: Int,
	private val maxDistanceY: Int,
	private val handler: ILightStartleHandler
) : AIBaseWanderConditioned(entity, movementSpeed){
	interface ILightStartleHandler{
		fun onLightStartled(): Boolean
		fun onDarknessReached()
	}
	
	private var lastLightLevel = 15
	private var wanderDelay = 0
	
	override fun shouldExecute(): Boolean{
		val prevBlockLight = lastLightLevel
		val newBlockLight = entity.world.getLightFor(BLOCK, Pos(entity))
		
		lastLightLevel = newBlockLight
		
		if (newBlockLight - prevBlockLight < minBlockLightIncrease){
			return false
		}
		
		if (!handler.onLightStartled()){
			return false
		}
		
		val newTarget = position ?: return false
		
		setTarget(newTarget)
		wanderDelay = entity.rng.nextInt(1, 4)
		return true
	}
	
	override fun shouldContinueExecuting(): Boolean{
		if (wanderDelay > 0 || super.shouldContinueExecuting()){
			return true
		}
		
		handler.onDarknessReached()
		return false
	}
	
	override fun getPosition(): Vec3d?{
		val world = entity.world
		val rand = entity.rng
		val nav = entity.navigator
		
		val startPos = Pos(entity)
		val startLight = getCombinedLight(startPos)
		
		var foundTarget: BlockPos? = null
		var foundLight = Int.MAX_VALUE
		
		for(attempt in 1..searchAttempts){
			val testPos = startPos.add(
				rand.nextInt(-maxDistanceXZ, maxDistanceXZ),
				rand.nextInt(-maxDistanceY, maxDistanceY),
				rand.nextInt(-maxDistanceXZ, maxDistanceXZ)
			)
			
			if (!testPos.isAir(world) || !nav.canEntityStandOnPos(testPos)){
				continue
			}
			
			val testLight = getCombinedLight(testPos)
			
			if (startLight - testLight >= minCombinedLightDecrease && testLight < foundLight && nav.func_179680_a(testPos, 0)?.let { validatePath(it, testPos) } == true){ // RENAME getPathToPos
				foundTarget = testPos
				foundLight = testLight
			}
		}
		
		return foundTarget?.let { Vec3d(it.x + 0.5, it.y.toDouble(), it.z + 0.5) }
	}
	
	private fun getCombinedLight(pos: BlockPos): Int{
		return entity.world.let { it.getLightFor(BLOCK, pos) + (it.getLightFor(SKY, pos) / 2) }
	}
	
	private fun validatePath(path: Path, target: BlockPos): Boolean{
		return path.finalPathPoint?.let { target.distanceTo(it.x, it.y, it.z) < 2.0 } == true
	}
	
	override fun startExecuting(){}
	
	override fun tick(){
		if (wanderDelay > 0 && --wanderDelay == 0){
			super.startExecuting()
		}
	}
	
	override fun resetTask(){
		lastLightLevel = entity.world.getLightFor(BLOCK, Pos(entity))
	}
}
