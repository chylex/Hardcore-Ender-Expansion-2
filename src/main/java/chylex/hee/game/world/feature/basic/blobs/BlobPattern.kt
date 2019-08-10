package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import java.util.Random

class BlobPattern private constructor(
	private val generators: WeightedList<IBlobGenerator>,
	private val populators: List<IPopulatorPicker>?
){
	constructor(generators: WeightedList<IBlobGenerator>) : this(generators, null)
	constructor(generators: WeightedList<IBlobGenerator>, populators: PopulatorBuilder) : this(generators, populators.build())
	
	constructor(generator: IBlobGenerator) : this(weightedListOf(1 to generator), null)
	constructor(generator: IBlobGenerator, populators: PopulatorBuilder) : this(weightedListOf(1 to generator), populators.build())
	
	fun pickGenerator(rand: Random): IBlobGenerator{
		return generators.generateItem(rand)
	}
	
	fun pickPopulators(rand: Random): List<IBlobPopulator>{
		val pickers = populators ?: return emptyList()
		val picked = mutableListOf<IBlobPopulator>()
		
		for(picker in pickers){
			picker.pick(rand, picked)
		}
		
		return picked
	}
	
	interface IPopulatorPicker{
		fun pick(rand: Random, list: MutableList<IBlobPopulator>)
	}
}
