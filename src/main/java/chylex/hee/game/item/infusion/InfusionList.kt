package chylex.hee.game.item.infusion
import chylex.hee.game.item.infusion.Infusion.VIGOR
import chylex.hee.system.collection.EmptyIterator
import chylex.hee.system.math.square
import chylex.hee.system.serialization.NBTEnumList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets

class InfusionList private constructor(private val infusions: ImmutableSet<Infusion>) : Iterable<Infusion>{
	constructor(infusion: Infusion, vararg moreInfusions: Infusion) : this(Sets.immutableEnumSet<Infusion>(infusion, *moreInfusions))
	constructor(list: NBTEnumList<Infusion>) : this(Sets.immutableEnumSet(list))
	
	companion object{
		val EMPTY = InfusionList(ImmutableSet.of())
	}
	
	val isEmpty
		get() = infusions.isEmpty()
	
	val size
		get() = infusions.size
	
	val tag
		get() = NBTEnumList.of(infusions)
	
	fun has(infusion: Infusion): Boolean{
		return infusions.contains(infusion)
	}
	
	fun with(infusion: Infusion): InfusionList{
		return InfusionList(infusion, *infusions.toTypedArray())
	}
	
	fun except(infusion: Infusion): InfusionList{
		return InfusionList(ImmutableSet.copyOf(infusions.minusElement(infusion)))
	}
	
	fun determineLevel(infusion: Infusion): Int{
		val vigor = has(VIGOR)
		val itself = has(infusion)
		
		return when{
			vigor && itself -> 2
			vigor || itself -> 1
			else -> 0
		}
	}
	
	fun calculateLevelMultiplier(infusion: Infusion, multiplier: Float): Float{
		return when(determineLevel(infusion)){
			2 -> square(multiplier)
			1 -> multiplier
			else -> 1F
		}
	}
	
	override fun iterator(): Iterator<Infusion>{
		return if (infusions.isEmpty())
			EmptyIterator.get()
		else
			infusions.sortedBy { it.ordinal }.iterator()
	}
}
