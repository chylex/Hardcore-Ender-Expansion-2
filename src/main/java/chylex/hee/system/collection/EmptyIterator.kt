package chylex.hee.system.collection

object EmptyIterator : MutableIterator<Any>{
	fun <T> get(): Iterator<T> = this as Iterator<T>
	
	override fun hasNext() = false
	
	override fun next() = throw NoSuchElementException("no element in empty iterator")
	override fun remove() = throw UnsupportedOperationException("cannot remove element from empty iterator")
}
