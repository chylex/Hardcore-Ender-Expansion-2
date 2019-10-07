package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.getState
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class AIPickUpBlock(
	private val entity: EntityCreature,
	private val ticksPerAttempt: Int,
	private val handler: IBlockPickUpHandler
) : EntityAIBase(){
	interface IBlockPickUpHandler{
		val canBeginSearch: Boolean
		fun onBeginSearch(): BlockPos?
		fun onBlockReached(state: IBlockState)
	}
	
	private var timeOfNextAttempt = entity.world.totalTime + entity.rng.nextInt(ticksPerAttempt / 2, ticksPerAttempt)
	
	private var targetNavPos: Vec3d? = null
	private var targetBlockPos: BlockPos? = null
	private var targetBlockState: IBlockState? = null
	
	override fun shouldExecute(): Boolean{
		if (entity.attackTarget != null || !handler.canBeginSearch){
			return false
		}
		
		val world = entity.world
		
		if (world.totalTime < timeOfNextAttempt){
			return false
		}
		
		resetTask()
		
		val pos = handler.onBeginSearch()
		
		if (pos == null){
			return false
		}
		
		val state = pos.getState(world)
		val nav = if (state.material.blocksMovement()) pos.up() else pos
		
		targetBlockPos = pos
		targetBlockState = state
		targetNavPos = Vec3d(nav.x + 0.5, nav.y.toDouble(), nav.z + 0.5)
		
		return true
	}
	
	override fun startExecuting(){
		val (x, y, z) = targetNavPos ?: return
		entity.navigator.tryMoveToXYZ(x, y, z, 1.0)
	}
	
	override fun shouldContinueExecuting(): Boolean{
		return !entity.navigator.noPath() && targetNavPos != null && targetBlockPos?.getState(entity.world) === targetBlockState
	}
	
	override fun updateTask(){
		val target = targetNavPos ?: return
		
		if (entity.posVec.squareDistanceTo(target) < square(1.33)){
			targetBlockPos?.let {
				val world = entity.world
				
				handler.onBlockReached(it.getState(world))
				it.breakBlock(world, false)
			}
			
			targetNavPos = null
		}
	}
	
	override fun resetTask(){
		timeOfNextAttempt = entity.world.totalTime + ticksPerAttempt
		targetBlockPos = null
		targetBlockState = null
	}
}
