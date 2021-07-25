package chylex.hee.game.entity.technical

import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getStringOrNull
import chylex.hee.util.nbt.use
import com.google.common.collect.HashBiMap
import net.minecraft.entity.EntityType
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable

class EntityTechnicalCausatumEvent(type: EntityType<EntityTechnicalCausatumEvent>, world: World) : EntityTechnicalBase(type, world) {
	constructor(world: World, handler: ICausatumEventHandler) : this(ModEntities.CAUSATUM_EVENT, world) {
		this.handler = handler
	}
	
	companion object {
		val TYPE = BaseType<EntityTechnicalCausatumEvent>()
		
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
		
		private val TYPE_MAPPING = HashBiMap.create(mapOf(
			map("EndermanKill", ::CausatumEventEndermanKill)
		))
		
		private inline fun <reified T : ICausatumEventHandler> map(name: String, noinline constructor: () -> T) = T::class.java to (name to constructor)
	}
	
	// Handler interface
	
	interface ICausatumEventHandler : INBTSerializable<TagCompound> {
		fun update(entity: EntityTechnicalCausatumEvent)
	}
	
	// Entity
	
	val type
		get() = handler::class.java
	
	private lateinit var handler: ICausatumEventHandler
	
	override fun registerData() {}
	
	override fun tick() {
		super.tick()
		
		if (!world.isRemote) {
			handler.update(this)
		}
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		val entry = TYPE_MAPPING[this@EntityTechnicalCausatumEvent.type]
		
		if (entry != null) {
			putString(TYPE_TAG, entry.first)
			put(DATA_TAG, handler.serializeNBT())
		}
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		val entry = getStringOrNull(TYPE_TAG)?.let { type -> TYPE_MAPPING.values.find { it.first == type } }
		
		if (entry != null) {
			handler = entry.second()
			handler.deserializeNBT(getCompound(DATA_TAG))
		}
		else {
			remove()
		}
	}
}
