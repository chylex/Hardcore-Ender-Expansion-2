package chylex.hee.system.component.general

import chylex.hee.system.component.EntityComponents
import chylex.hee.system.serialization.TagCompound
import net.minecraftforge.common.util.INBTSerializable

interface SerializableComponent : INBTSerializable<TagCompound> {
	val serializationKey: String
}

fun EntityComponents.serializeTo(tag: TagCompound) {
	this.on<SerializableComponent> {
		require(!tag.contains(serializationKey)) { "[SerializableComponent] cannot serialize duplicate key: $serializationKey" }
		tag.put(serializationKey, serializeNBT())
	}
}

fun EntityComponents.deserializeFrom(tag: TagCompound) {
	this.on<SerializableComponent> { deserializeNBT(tag.getCompound(serializationKey)) }
}
