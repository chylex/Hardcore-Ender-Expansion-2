package chylex.hee.system.component

abstract class AbstractAwareComponent {
	var entityComponents: EntityComponents? = null
	
	open fun onComponentAttached() {}
	open fun onComponentDetached() {}
}
