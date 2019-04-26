package chylex.hee.game.world.feature.basic.ores.impl
import chylex.hee.game.world.feature.basic.ores.IOreTechnique
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.nextVector
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.pow

class OreTechniqueDistance(
	private val oresPerCluster: (Random) -> Int,
	private val maxDistance: Double,
	private val powDistance: Double = 1.0
) : IOreTechnique{
	override fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean{
		val rand = world.rand
		val ores = oresPerCluster(rand).takeIf { it > 0 } ?: return true
		
		if (!placer.place(world, pos)){
			return false
		}
		
		val center = pos.center
		var generatedAny = false
		
		repeat(ores){
			for(attempt in 1..20){
				val next = Pos(center.add(rand.nextVector(rand.nextDouble().pow(powDistance) * maxDistance)))
				
				if (placer.place(world, next)){
					generatedAny = true
					break
				}
			}
		}
		
		return generatedAny
	}
}
