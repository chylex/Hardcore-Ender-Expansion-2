package chylex.hee.game.world.generation.blob

import chylex.hee.game.world.generation.blob.BlobSmoothing.FULL
import chylex.hee.game.world.generation.blob.BlobSmoothing.MILD
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.allInCenteredSphereMutable
import chylex.hee.game.world.util.max
import chylex.hee.util.math.ceilToInt
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

class BlobGenerator(val base: Block) {
	companion object {
		val END_STONE = BlobGenerator(Blocks.END_STONE)
	}
	
	fun place(world: SegmentedWorld, center: BlockPos, radius: Double): Boolean {
		val offset = radius.ceilToInt()
		
		if (!world.isInside(center) || Facing6.any { !world.isInside(center.offset(it, offset)) }) {
			return false
		}
		
		for (pos in center.allInCenteredSphereMutable(radius)) {
			world.setBlock(pos, base)
		}
		
		return true
	}
	
	fun generate(world: SegmentedWorld, rand: Random, center: BlockPos, smoothing: BlobSmoothing, pattern: BlobPattern): Boolean {
		val layout = pattern.pickLayout(rand)
		val populators = pattern.pickPopulators(rand)
		
		val extraSize = populators.fold(BlockPos.ZERO) { acc, populator -> acc.max(populator.expandSizeBy) }
		val allocatedSize = layout.size.expand(extraSize)
		
		val origin = center.subtract(allocatedSize.centerPos)
		
		if (!allocatedSize.toBoundingBox(origin).isInside(world.worldSize.toBoundingBox(BlockPos.ZERO))) {
			return false
		}
		
		val blobWorld = ScaffoldedWorld(rand, allocatedSize)
		
		layout.generate(blobWorld, rand, this)
		
		if (smoothing == FULL) {
			runSmoothingPass(blobWorld, adjacentAirCount = 4)
		}
		
		if (smoothing == FULL || smoothing == MILD) {
			runSmoothingPass(blobWorld, adjacentAirCount = 5)
		}
		
		for (populator in populators) {
			populator.generate(blobWorld, rand, this)
		}
		
		blobWorld.cloneInto(world, origin)
		return true
	}
	
	private fun runSmoothingPass(blobWorld: ScaffoldedWorld, adjacentAirCount: Int) {
		for (pos in blobWorld.allPositionsMutable) {
			if (blobWorld.getBlock(pos) === base && Facing6.count { facing -> pos.offset(facing).let { !blobWorld.isInside(it) || blobWorld.isUnused(it) } } >= adjacentAirCount) {
				blobWorld.markUnused(pos)
			}
		}
	}
}
