package chylex.hee.game.world.feature.basic.caves
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class CaveGenerator(
	private val carver: ICaveCarver,
	private val radius: ICaveRadius,
	private val placer: IBlockPlacer = BlockReplacer(fill = Blocks.AIR, replace = Blocks.END_STONE),
	private val stepSize: Double
){
	fun generate(world: SegmentedWorld, start: Vec3d, length: Double, pather: ICavePather): Int{
		val rand = world.rand
		val worldBox = world.worldSize.toBoundingBox(BlockPos.ORIGIN)
		
		val steps = (length / stepSize).ceilToInt()
		var point = start
		
		var stepCounter = 0
		var failCounter = 0
		var successfulSteps = 0
		
		while(stepCounter < steps){
			point = point.add(pather.nextOffset(rand, point).normalize().scale(stepSize))
			
			val nextRadius = radius.next(rand, stepCounter)
			val carveBox = Size(nextRadius.ceilToInt()).toBoundingBox(Pos(point))
			
			if (carveBox.intersects(worldBox) && carver.carve(world, point, nextRadius, placer)){
				++successfulSteps
				++stepCounter
			}
			else if (++failCounter == 4){
				failCounter = 0
				++stepCounter
			}
		}
		
		return successfulSteps
	}
}
