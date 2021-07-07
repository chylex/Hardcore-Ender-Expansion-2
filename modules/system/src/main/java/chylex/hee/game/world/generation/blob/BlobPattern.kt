package chylex.hee.game.world.generation.blob

import chylex.hee.util.collection.WeightedList
import chylex.hee.util.collection.weightedListOf
import java.util.Random

class BlobPattern private constructor(
	private val layouts: WeightedList<IBlobLayout>,
	private val populators: List<IPopulatorPicker>?,
) {
	constructor(layouts: WeightedList<IBlobLayout>) : this(layouts, null)
	constructor(layouts: WeightedList<IBlobLayout>, populators: PopulatorBuilder) : this(layouts, populators.build())
	
	constructor(layout: IBlobLayout) : this(weightedListOf(1 to layout), null)
	constructor(layout: IBlobLayout, populators: PopulatorBuilder) : this(weightedListOf(1 to layout), populators.build())
	
	fun pickLayout(rand: Random): IBlobLayout {
		return layouts.generateItem(rand)
	}
	
	fun pickPopulators(rand: Random): List<IBlobPopulator> {
		val pickers = populators ?: return emptyList()
		val picked = mutableListOf<IBlobPopulator>()
		
		for (picker in pickers) {
			picker.pick(rand, picked)
		}
		
		return picked
	}
	
	interface IPopulatorPicker {
		fun pick(rand: Random, list: MutableList<IBlobPopulator>)
	}
}
