package chylex.hee.game.entity.technical
import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getStringOrNull
import chylex.hee.system.util.heeTag
import com.google.common.collect.HashBiMap
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable

class EntityTechnicalCausatumEvent(world: World) : EntityTechnicalBase(world){
	constructor(world: World, handler: ICausatumEventHandler) : this(world){
		this.handler = handler
	}
	
	private companion object{
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
		
		private val TYPE_MAPPING = HashBiMap.create(mapOf(
			map("EndermanKill", ::CausatumEventEndermanKill)
		))
		
		private inline fun <reified T : ICausatumEventHandler> map(name: String, noinline constructor: () -> T) = T::class.java to (name to constructor)
	}
	
	// Handler interface
	
	interface ICausatumEventHandler : INBTSerializable<TagCompound>{
		fun update(entity: EntityTechnicalCausatumEvent)
	}
	
	// Entity
	
	val type
		get() = handler::class.java
	
	private lateinit var handler: ICausatumEventHandler
	
	override fun entityInit(){}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (!world.isRemote){
			handler.update(this)
		}
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		val entry = TYPE_MAPPING[type]
		
		if (entry != null){
			setString(TYPE_TAG, entry.first)
			setTag(DATA_TAG, handler.serializeNBT())
		}
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		val entry = getStringOrNull(TYPE_TAG)?.let { type -> TYPE_MAPPING.values.find { it.first == type } }
		
		if (entry != null){
			handler = entry.second()
			handler.deserializeNBT(getCompoundTag(DATA_TAG))
		}
		else{
			setDead()
		}
	}
}
