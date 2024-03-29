package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getState
import chylex.hee.util.math.bottomCenter
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import chylex.hee.util.math.square
import chylex.hee.util.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d

class AIPickUpBlock(
	private val entity: CreatureEntity,
	private val ticksPerAttempt: Int,
	private val handler: IBlockPickUpHandler,
) : Goal() {
	interface IBlockPickUpHandler {
		val canBeginSearch: Boolean
		fun onBeginSearch(): BlockPos?
		fun onBlockReached(state: BlockState)
	}
	
	private var timeOfNextAttempt = entity.world.gameTime + entity.rng.nextInt(ticksPerAttempt / 2, ticksPerAttempt)
	
	private var targetNavPos: Vector3d? = null
	private var targetBlockPos: BlockPos? = null
	private var targetBlockState: BlockState? = null
	
	override fun shouldExecute(): Boolean {
		if (entity.attackTarget != null || !handler.canBeginSearch) {
			return false
		}
		
		val world = entity.world
		
		if (world.gameTime < timeOfNextAttempt) {
			return false
		}
		
		resetTask()
		
		val pos = handler.onBeginSearch()
		
		if (pos == null) {
			return false
		}
		
		val state = pos.getState(world)
		val nav = if (state.material.blocksMovement()) pos.up() else pos
		
		targetBlockPos = pos
		targetBlockState = state
		targetNavPos = nav.bottomCenter
		
		return true
	}
	
	override fun startExecuting() {
		val (x, y, z) = targetNavPos ?: return
		entity.navigator.tryMoveToXYZ(x, y, z, 1.0)
	}
	
	override fun shouldContinueExecuting(): Boolean {
		return !entity.navigator.noPath() && targetNavPos != null && targetBlockPos?.getState(entity.world) === targetBlockState
	}
	
	override fun tick() {
		val target = targetNavPos ?: return
		
		if (entity.posVec.squareDistanceTo(target) < square(1.33)) {
			targetBlockPos?.let {
				val world = entity.world
				
				handler.onBlockReached(it.getState(world))
				it.breakBlock(world, false)
			}
			
			targetNavPos = null
		}
	}
	
	override fun resetTask() {
		timeOfNextAttempt = entity.world.gameTime + ticksPerAttempt
		targetBlockPos = null
		targetBlockState = null
	}
}
