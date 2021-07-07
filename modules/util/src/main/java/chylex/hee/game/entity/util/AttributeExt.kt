package chylex.hee.game.entity.util

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attribute
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.event.entity.EntityAttributeCreationEvent

// Attributes (Forge)

val ENTITY_GRAVITY
	get() = ForgeMod.ENTITY_GRAVITY.get()

val REACH_DISTANCE
	get() = ForgeMod.REACH_DISTANCE.get()

// Attributes (Operations)

/** Performs operation: base + x + y */
val OP_ADD = Operation.ADDITION

/** Performs operation: base * (1 + x + y) */
val OP_MUL_INCR_GROUPED = Operation.MULTIPLY_BASE

/** Performs operation: base * (1 + x) * (1 + y) */
val OP_MUL_INCR_INDIVIDUAL = Operation.MULTIPLY_TOTAL

// Attributes (Helpers)

fun LivingEntity.getAttributeInstance(attribute: Attribute): ModifiableAttributeInstance {
	return this.getAttribute(attribute)!!
}

fun ModifiableAttributeInstance.tryApplyPersistentModifier(modifier: AttributeModifier) {
	if (!this.hasModifier(modifier)) {
		this.applyPersistentModifier(modifier)
	}
}

fun ModifiableAttributeInstance.tryApplyNonPersistentModifier(modifier: AttributeModifier) {
	if (!this.hasModifier(modifier)) {
		this.applyNonPersistentModifier(modifier)
	}
}

fun ModifiableAttributeInstance.tryRemoveModifier(modifier: AttributeModifier) {
	if (this.hasModifier(modifier)) {
		this.removeModifier(modifier)
	}
}

fun MutableAttribute.with(vararg attributeValues: Pair<Attribute, Double>) = apply {
	for ((attribute, value) in attributeValues) {
		this.createMutableAttribute(attribute, value)
	}
}

operator fun EntityAttributeCreationEvent.set(entityType: EntityType<out LivingEntity>, attributeBuilder: MutableAttribute) {
	this.put(entityType, attributeBuilder.create())
}
