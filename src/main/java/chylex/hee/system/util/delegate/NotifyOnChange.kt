package chylex.hee.system.util.delegate
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 * Variation of [Delegates.observable][kotlin.properties.Delegates.observable] that only executes [onChange] if the newValue is not equal to oldValue.
 */
class NotifyOnChange<T>(initialValue: T, private val onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit) : ObservableProperty<T>(initialValue){
	constructor(initialValue: T, onChange: (T) -> Unit) : this(initialValue, { _, _, newValue -> onChange(newValue) })
	constructor(initialValue: T, onChange: () -> Unit) : this(initialValue, { _, _, _ -> onChange() })
	
	override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T){
		if (newValue != oldValue){
			onChange(property, oldValue, newValue)
		}
	}
}
