package chylex.hee.game.item.infusion
import chylex.hee.system.collection.EmptyIterator
import chylex.hee.system.util.NBTEnumList
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
	
	val tag
		get() = NBTEnumList.of(infusions)
	
	fun has(infusion: Infusion): Boolean{
		return infusions.contains(infusion)
	}
	
	fun with(infusion: Infusion): InfusionList{
		return InfusionList(infusion, *infusions.toTypedArray())
	}
	
	override fun iterator(): Iterator<Infusion>{
		return if (infusions.isEmpty())
			EmptyIterator.get()
		else
			infusions.asSequence().sortedBy { it.ordinal }.iterator()
	}
}
