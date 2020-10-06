package chylex.hee.system.migration
import java.util.function.Supplier

fun <T> supply(thing: T) = Supplier { thing }
