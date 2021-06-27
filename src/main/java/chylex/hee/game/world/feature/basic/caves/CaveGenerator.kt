package chylex.hee.game.world.feature.basic.caves

import chylex.hee.game.world.Pos
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.math.Size
import chylex.hee.system.math.ceilToInt
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d

class CaveGenerator(
	private val carver: ICaveCarver,
	private val radius: ICaveRadius,
	private val placer: IBlockPlacer = BlockReplacer(fill = Blocks.AIR, replace = Blocks.END_STONE),
	private val stepSize: Double,
	private val maxConsecutiveFails: Int = Int.MAX_VALUE,
) {
	fun generate(world: SegmentedWorld, start: Vector3d, length: Double, pather: ICavePather): Int {
		val rand = world.rand
		val worldBox = world.worldSize.toBoundingBox(BlockPos.ZERO)
		
		val steps = (length / stepSize).ceilToInt()
		var point = start
		
		var stepCounter = 0
		var failCounter = 0
		var successfulSteps = 0
		var failsLeft = maxConsecutiveFails
		
		while(stepCounter < steps) {
			point = point.add(pather.nextOffset(rand, point, stepSize))
			
			val nextRadius = radius.next(rand, stepCounter)
			val carveBox = Size(nextRadius.ceilToInt()).toBoundingBox(Pos(point))
			
			if (carveBox.intersects(worldBox) && carver.carve(world, point, nextRadius, placer)) {
				++successfulSteps
				++stepCounter
				failsLeft = maxConsecutiveFails
			}
			else {
				if (--failsLeft < 0) {
					break
				}
				else if (++failCounter == 4) {
					failCounter = 0
					++stepCounter
				}
			}
		}
		
		return successfulSteps
	}
}
