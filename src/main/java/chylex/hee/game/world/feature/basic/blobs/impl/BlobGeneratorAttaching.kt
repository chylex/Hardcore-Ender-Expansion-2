package chylex.hee.game.world.feature.basic.blobs.impl
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobGenerator
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.nextVector
import net.minecraft.util.math.BlockPos
import java.util.Random

open class BlobGeneratorAttaching(
	private val amount: (Random) -> Int,
	private val radius: (Int, Random) -> Double,
	private val distance: (Random) -> Double,
	private val strategy: AttachingStrategy,
	maxSize: Int
) : IBlobGenerator{
	constructor(
		amount: (Random) -> Int,
		radiusFirst: (Random) -> Double,
		radiusOther: (Random) -> Double,
		distance: (Random) -> Double,
		strategy: AttachingStrategy,
		maxSize: Int
	) : this(
		amount,
		{ index, rand -> (if (index == 0) radiusFirst else radiusOther)(rand) },
		distance,
		strategy,
		maxSize
	)
	
	enum class AttachingStrategy{
		FIRST_BLOB{
			override fun nextIndex(generated: List<Pair<BlockPos, Double>>, rand: Random): Int{
				return 0
			}
		},
		
		LAST_BLOB{
			override fun nextIndex(generated: List<Pair<BlockPos, Double>>, rand: Random): Int{
				return generated.lastIndex
			}
		},
		
		RANDOM_BLOB{
			override fun nextIndex(generated: List<Pair<BlockPos, Double>>, rand: Random): Int{
				return rand.nextInt(generated.size)
			}
		};
		
		abstract fun nextIndex(generated: List<Pair<BlockPos, Double>>, rand: Random): Int
	}
	
	override val size = Size(maxSize)
	
	override fun generate(world: SegmentedWorld, rand: Random){
		val totalBlobs = amount(rand).takeIf { it > 0 } ?: return
		
		val generated = mutableListOf(
			world.worldSize.centerPos to radius(0, rand)
		)
		
		if (!BlobGenerator.place(world, generated[0].first, generated[0].second)){
			return
		}
		
		repeat(totalBlobs - 1){
			for(attempt in 1..5){
				val (attachPos, attachRad) = generated[strategy.nextIndex(generated, rand)]
				
				val nextRad = radius(1 + it, rand)
				val nextDistance = (attachRad + nextRad) * distance(rand)
				
				if (BlobGenerator.place(world, Pos(attachPos.center.add(rand.nextVector(nextDistance))), nextRad)){
					break
				}
			}
		}
	}
}
