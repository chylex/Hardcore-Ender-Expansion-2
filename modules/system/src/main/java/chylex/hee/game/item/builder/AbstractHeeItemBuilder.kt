package chylex.hee.game.item.builder

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import net.minecraft.item.Food
import net.minecraft.item.Item
import net.minecraft.item.Item.Properties
import net.minecraft.tags.ITag.INamedTag

abstract class AbstractHeeItemBuilder<T : Item> {
	var localization: LocalizationStrategy? = null
	val localizationExtra = mutableMapOf<String, String>()
	
	var model: ItemModel? = null
	var tint: ItemTint? = null
	val properties = mutableListOf<ItemProperty>()
	
	var maxStackSize: Int? = null
	var maxDamage: Int? = null
	var food: Food? = null
	var immuneToFire = false
	var noRepair = false
	
	private val lazyComponents = lazy(::HeeItemComponents)
	val components
		get() = lazyComponents.value
	
	val tags = mutableListOf<INamedTag<Item>>()
	val interfaces = HeeItemInterfaces()
	val callbacks = mutableListOf<Item.() -> Unit>()
	
	fun includeFrom(source: AbstractHeeItemBuilder<*>) {
		source.localization?.let { this.localization = it }
		this.localizationExtra.putAll(source.localizationExtra)
		
		source.model?.let { this.model = it }
		source.tint?.let { this.tint = it }
		this.properties.addAll(source.properties)
		
		source.maxStackSize?.let { this.maxStackSize = it }
		source.maxDamage?.let { this.maxDamage = it }
		source.food?.let { this.food = it }
		this.immuneToFire = source.immuneToFire
		this.noRepair = source.noRepair
		
		if (source.lazyComponents.isInitialized()) {
			this.components.includeFrom(source.components)
		}
		
		this.tags.addAll(source.tags)
		this.interfaces.includeFrom(source.interfaces)
		this.callbacks.addAll(source.callbacks)
	}
	
	private fun buildProperties(setup: ((Properties) -> Properties)?): Properties {
		var properties = Properties()
		
		if (setup != null) {
			properties = setup(properties)
		}
		
		properties = properties.apply(maxStackSize, Properties::maxStackSize)
		properties = properties.apply(maxDamage, Properties::maxDamage)
		properties = properties.apply(food, Properties::food)
		
		if (immuneToFire) {
			properties = properties.isImmuneToFire
		}
		
		if (noRepair) {
			properties = properties.setNoRepair()
		}
		
		return properties
	}
	
	private inline fun <T> Properties.apply(value: T?, function: Properties.(T) -> Properties): Properties {
		return if (value == null) this else function(value)
	}
	
	fun build(propertiesSetup: (Properties.() -> Properties)? = null): T {
		val components = if (lazyComponents.isInitialized()) components else null
		val item = buildItem(buildProperties(propertiesSetup), components)
		
		for (callback in callbacks) {
			callback(item)
		}
		
		return item
	}
	
	internal abstract fun buildItem(properties: Properties, components: HeeItemComponents?): T
}
