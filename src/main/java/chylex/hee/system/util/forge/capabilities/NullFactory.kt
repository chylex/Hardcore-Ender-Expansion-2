package chylex.hee.system.util.forge.capabilities
import java.util.concurrent.Callable

object NullFactory : Callable<Any>{
	fun <T> get(): Callable<T> = this as Callable<T>
	
	override fun call(): Any{
		throw UnsupportedOperationException("no default capability implementation")
	}
}
