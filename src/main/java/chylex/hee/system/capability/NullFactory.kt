package chylex.hee.system.capability
import java.util.concurrent.Callable

object NullFactory : Callable<Any>{
	@Suppress("UNCHECKED_CAST")
	fun <T> get(): Callable<T> = this as Callable<T>
	
	override fun call(): Any{
		throw UnsupportedOperationException("no default capability implementation")
	}
}
