package chylex.hee.util.forge

import java.util.function.Supplier

fun <T> supply(thing: T) = Supplier { thing }
