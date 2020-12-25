package chylex.hee.game.world.feature.basic.blobs

import chylex.hee.game.world.feature.basic.blobs.BlobPattern.IPopulatorPicker
import chylex.hee.system.collection.WeightedList
import java.util.Random

class PopulatorBuilder {
	private val list = mutableListOf<IPopulatorPicker>()
	
	fun custom(picker: IPopulatorPicker) {
		list.add(picker)
	}
	
	fun guarantee(vararg populators: IBlobPopulator) {
		list.add(Guarantee(*populators))
	}
	
	fun pick(options: WeightedList<IBlobPopulator>, amount: (Random) -> Int, keepOrder: Boolean) {
		if (keepOrder) {
			list.add(PickOrdered(options, amount))
		}
		else {
			list.add(PickUnordered(options, amount))
		}
	}
	
	fun shuffle() {
		list.add(Shuffle)
	}
	
	fun build(): List<IPopulatorPicker> {
		return list.toList()
	}
	
	// Implementations
	
	private class Guarantee(private vararg val populators: IBlobPopulator) : IPopulatorPicker {
		override fun pick(rand: Random, list: MutableList<IBlobPopulator>) {
			list.addAll(populators)
		}
	}
	
	private class PickOrdered(private val options: WeightedList<IBlobPopulator>, private val amount: (Random) -> Int) : IPopulatorPicker {
		override fun pick(rand: Random, list: MutableList<IBlobPopulator>) {
			val total = amount(rand)
			
			if (total > 0) {
				val remaining = options.mutableCopy()
				val picked = mutableListOf<IBlobPopulator>()
				
				repeat(total) {
					remaining.removeItem(rand)?.let(picked::add)
				}
				
				list.addAll(picked.sortedBy(options.values::indexOf))
			}
		}
	}
	
	private class PickUnordered(private val options: WeightedList<IBlobPopulator>, private val amount: (Random) -> Int) : IPopulatorPicker {
		override fun pick(rand: Random, list: MutableList<IBlobPopulator>) {
			val total = amount(rand)
			
			if (total > 0) {
				val remaining = options.mutableCopy()
				
				repeat(total) {
					remaining.removeItem(rand)?.let(list::add)
				}
			}
		}
	}
	
	private object Shuffle : IPopulatorPicker {
		override fun pick(rand: Random, list: MutableList<IBlobPopulator>) {
			list.shuffle(rand)
		}
	}
}
