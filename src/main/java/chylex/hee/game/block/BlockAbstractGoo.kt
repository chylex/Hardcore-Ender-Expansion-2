package chylex.hee.game.block

import chylex.hee.game.block.fluid.FlowingFluid5
import chylex.hee.game.block.fluid.FluidBase
import chylex.hee.game.block.util.BlockCollisionLimiter
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.game.world.util.getState
import chylex.hee.system.heeTag
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.supply
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.getLongOrNull
import chylex.hee.util.nbt.getOrCreateCompound
import net.minecraft.block.BlockState
import net.minecraft.block.FlowingFluidBlock
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

abstract class BlockAbstractGoo(
	private val fluid: FluidBase,
	material: Material,
) : FlowingFluidBlock(supply(fluid.still), Properties.create(material, fluid.mapColor).hardnessAndResistance(fluid.resistance).doesNotBlockMovement().noDrops()) {
	protected companion object {
		private const val LAST_TIME_TAG = "Time"
		private const val TOTAL_TICKS_TAG = "Ticks"
	}
	
	// Initialization
	
	private var collisionLimiter = BlockCollisionLimiter()
	
	protected abstract val tickTrackingKey: String
	
	// Behavior
	
	final override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		if (collisionLimiter.check(world, entity)) {
			// handling from Entity.doBlockCollisions
			val bb = entity.boundingBox
			val posMin = Pos(bb.minX - 0.001, bb.minY - 0.001, bb.minZ - 0.001)
			val posMax = Pos(bb.maxX + 0.001, bb.maxY + 0.001, bb.maxZ + 0.001)
			
			var lowestLevel = Int.MAX_VALUE
			
			for (testPos in posMin.allInBoxMutable(posMax)) {
				val level = testPos.getState(world).takeIf { it.block === this }?.let { FlowingFluid5.stateToLevel(it) } ?: continue
				
				if (level < lowestLevel) {
					lowestLevel = level
				}
			}
			
			if (lowestLevel != Int.MAX_VALUE) {
				if (!world.isRemote) {
					onInsideGoo(entity)
				}
				
				if (!(entity is PlayerEntity && entity.abilities.isFlying)) {
					modifyMotion(entity, lowestLevel)
				}
			}
		}
	}
	
	protected fun trackTick(entity: Entity, maxTicks: Int): Int {
		val world = entity.world
		val currentWorldTime = world.gameTime
		
		with(entity.heeTag.getOrCreateCompound(tickTrackingKey)) {
			val lastWorldTime = getLongOrNull(LAST_TIME_TAG) ?: (currentWorldTime - 1)
			var totalTicks = getInt(TOTAL_TICKS_TAG)
			
			val ticksSinceLastUpdate = currentWorldTime - lastWorldTime
			
			if (ticksSinceLastUpdate > 1L) {
				totalTicks = (totalTicks - (ticksSinceLastUpdate / 2).toInt()).coerceAtLeast(0)
			}
			
			if (totalTicks < maxTicks && world.rand.nextInt(10) != 0) {
				++totalTicks
			}
			
			putLong(LAST_TIME_TAG, currentWorldTime)
			putInt(TOTAL_TICKS_TAG, totalTicks)
			
			return totalTicks
		}
	}
	
	abstract fun onInsideGoo(entity: Entity)
	abstract fun modifyMotion(entity: Entity, level: Int)
	
	@Sided(Side.CLIENT)
	override fun getFogColor(state: BlockState, world: IWorldReader, pos: BlockPos, entity: Entity, originalColor: Vector3d, partialTicks: Float): Vector3d {
		return fluid.fogColor
	}
}
