package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.util.nextInt
import java.util.Random

class BlobPattern private constructor(
	private val generators: WeightedList<IBlobGenerator>,
	private val populators: Pair<WeightedList<IBlobPopulator>, ((Random) -> Int)>?
){
	constructor(generators: WeightedList<IBlobGenerator>) : this(generators, null)
	
	constructor(generators: WeightedList<IBlobGenerator>, populators: WeightedList<IBlobPopulator>, populatorAmount: (Random) -> Int) : this(
		generators, populators to populatorAmount
	)

	constructor(generators: WeightedList<IBlobGenerator>, populators: WeightedList<IBlobPopulator>, populatorAmount: IntRange) : this(
		generators, populators to { rand -> rand.nextInt(populatorAmount.start, populatorAmount.endInclusive) }
	)
	
	fun pickGenerator(rand: Random): IBlobGenerator{
		return generators.generateItem(rand)
	}
	
	fun pickPopulators(rand: Random): List<IBlobPopulator>{
		val (list, amount) = populators ?: return emptyList()
		
		val remaining = list.mutableCopy()
		val picked = mutableListOf<IBlobPopulator>()
		
		repeat(amount(rand)){
			remaining.removeItem(rand)?.let(picked::add)
		}
		
		return picked
	}
}
