package chylex.hee.system.component

class EntityComponents {
	private val mutableComponents = mutableListOf<Any>()
	
	val components: List<Any>
		get() = mutableComponents
	
	fun attach(component: Any) {
		require(!mutableComponents.contains(component)) { "[EntityComponents] component must not be registered twice" }
		mutableComponents.add(component)
		
		if (component is AbstractAwareComponent) {
			require(component.entityComponents === null) { "[EntityComponents] component must not be registered in two entities" }
			component.entityComponents = this
			component.onComponentAttached()
		}
	}
	
	fun detach(component: Any) {
		if (!mutableComponents.remove(component)) {
			return
		}
		
		if (component is AbstractAwareComponent) {
			require(component.entityComponents === this) { "[EntityComponents] component was not registered correctly" }
			component.onComponentDetached()
			component.entityComponents = null
		}
	}
	
	inline fun <reified T> on(f: T.() -> Unit) {
		for(component in components) {
			if (component is T) {
				f(component)
			}
		}
	}
	
	inline fun <reified T, U> handle(f: T.() -> U?): U? {
		for(component in components) {
			if (component is T) {
				val result = f(component)
				if (result != null) {
					return result
				}
			}
		}
		
		return null
	}
	
	inline fun <reified T> list(): List<T> {
		return components.filterIsInstance<T>()
	}
}
