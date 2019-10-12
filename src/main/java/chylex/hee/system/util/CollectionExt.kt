package chylex.hee.system.util

inline fun <T> Iterator<T>.any(predicate: (T) -> Boolean): Boolean{
	for(element in this){
		if (predicate(element)){
			return true
		}
	}
	
	return false
}

inline fun <T> Iterator<T>.find(predicate: (T) -> Boolean): T?{
	for(element in this){
		if (predicate(element)){
			return element
		}
	}
	
	return null
}
